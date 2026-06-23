package com.steven.solomon.rabbitmq.consumer;

import cn.hutool.core.util.StrUtil;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.steven.solomon.rabbitmq.annotation.MessageListener;
import com.steven.solomon.rabbitmq.annotation.MessageListenerRetry;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.rabbitmq.entity.RabbitMqModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.mq.idempotency.DuplicateMessageException;
import com.steven.solomon.mq.idempotency.MessageIdempotencyExecutor;
import com.steven.solomon.mq.MessageListenerSpi;
import com.steven.solomon.pojo.vo.ResultVO;
import com.steven.solomon.rabbitmq.utils.RabbitUtils;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ 消费者基类。
 *
 * <p>继承 Spring AMQP 的 {@link MessageListenerAdapter}，并实现 MQ 公共消费 SPI
 * {@link MessageListenerSpi}。统一处理消息反序列化、幂等校验、手动 ACK、
 * 失败重试计数、请求-回应回调和消费日志。</p>
 *
 * <p>子类只需实现 {@link #handleMessage(Object)} 处理业务逻辑，
 * 并通过 {@link MessageListener} / {@link MessageListenerRetry} 注解配置消费行为。</p>
 *
 * @param <T> 业务消息体类型
 * @param <R> 消费结果类型
 * @author steven
 */
public abstract class AbstractConsumer<T, R> extends MessageListenerAdapter implements MessageListenerSpi<T,R,RabbitMqModel<T>> {

    protected final Logger logger = LoggerUtils.logger(getClass());

    /** 默认重试次数（注解未配置时使用）。 */
    private final int defaultRetryNumber = 1;

    /** RPC 请求-回应场景的关联ID。 */
    protected String correlationId;

    /** 当前消息的 AMQP 属性，用于获取 deliveryTag 等。 */
    protected MessageProperties messageProperties;

    /** RabbitMQ 工具类，提供发送、回应等能力。 */
    protected final RabbitUtils rabbitUtils;

    /** 配置的最大重试次数。 */
    protected final int retryNumber;

    /** 是否自动确认（AUTO 模式下框架自动 ACK，否则手动 ACK）。 */
    protected final boolean isAutoAck;

    /** 当前消息解析出的租户编码。 */
    protected String tenantCode;

    private volatile MessageIdempotencyExecutor cachedMessageIdempotencyExecutor;

    protected AbstractConsumer(RabbitUtils rabbitUtils) {
        this.rabbitUtils = rabbitUtils;
        MessageListenerRetry messageListenerRetry = getClass().getAnnotation(MessageListenerRetry.class);
        MessageListener messageListener = getClass().getAnnotation(MessageListener.class);
        this.retryNumber = ObjectUtil.isEmpty(messageListenerRetry) ? defaultRetryNumber : messageListenerRetry.retryNumber();
        this.isAutoAck = ObjectUtil.isNotNull(messageListener)
                && StrUtil.equalsIgnoreCase(AcknowledgeMode.AUTO.toString(), messageListener.mode().toString());
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        setProperties(message);
        // 提取消息体为字符串，便于反序列化和日志记录
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        RabbitMqModel<T> model = null;
        Throwable throwable = null;
        R result = null;
        try {
            logger.info("线程名:{}, RabbitMQ 消费者收到消息: {}", Thread.currentThread().getName(), json);
            model = conversion(json);
            tenantCode = model.getTenantCode();
            // 幂等校验：消息已消费过则直接抛异常
            if (checkMessageKey(model)) {
                throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
            }
            // 绑定租户上下文，供下游资源（数据源/缓存等）按租户切换
            if (ObjectUtil.isNotEmpty(tenantCode)) {
                RequestHeaderHolder.setTenantCode(tenantCode);
            }
            MessageIdempotencyExecutor idempotencyExecutor = messageIdempotencyExecutor();
            String idempotencyKey = messageIdempotencyKey(model);
            if (idempotencyExecutor != null && StrUtil.isNotBlank(idempotencyKey)) {
                RabbitMqModel<T> messageModel = model;
                result = idempotencyExecutor.execute(idempotencyKey, () -> this.handleMessage(messageModel.getBody()));
            } else {
                result = this.handleMessage(model.getBody());
            }
            // 非自动确认模式下，消费成功后手动 ACK
            if (!isAutoAck) {
                channel.basicAck(messageProperties.getDeliveryTag(), false);
            }
            // RPC 场景：将处理结果回发到 replyTo 队列
            sendReplyTo(result);
        } catch (DuplicateMessageException e) {
            logger.info("RabbitMQ duplicate message skipped, tenant={}, msgId={}, deliveryTag={}",
                    tenantCode, model == null ? null : model.getMsgId(), messageProperties.getDeliveryTag());
            throwable = e;
            if (!isAutoAck) {
                channel.basicAck(messageProperties.getDeliveryTag(), false);
            }
            return;
        } catch (Throwable e) {
            // 失败时按重试次数决定 ACK/NACK 并更新重试计数
            saveFailNumber(channel, e);
            throwable = e;
            throw e;
        } finally {
            // 释放幂等校验 Key
            deleteCheckMessageKey(model);
            // 记录消费日志（成功或失败）
            saveLog(result, throwable, model);
        }
    }

    private void setProperties(Message message) {
        messageProperties = message.getMessageProperties();
        correlationId = messageProperties.getHeader("spring_listener_return_correlation");
    }

    /**
     * 记录失败次数并决定是否拒绝此消息。
     *
     * <p>重试次数达到上限后调用 {@code basicNack} 拒绝消息（不重新入队），
     * 否则递增重试计数头，让消息重新投递继续重试。</p>
     */
    public void saveFailNumber(Channel channel, Throwable e) throws Exception {
        logger.error("RabbitMQ 消费失败, 异常信息:", e);

        Integer retryCount = messageProperties.getHeader("retryNumber");
        int actualRetry = ObjectUtil.isEmpty(retryCount) ? 1 : retryCount + 1;
        logger.error("RabbitMQ 失败记录: 关联ID={}, deliveryTag={}, 已重试次数={}",
                correlationId, messageProperties.getDeliveryTag(), actualRetry);

        if (retryNumber <= this.defaultRetryNumber || actualRetry >= retryNumber) {
            if (retryNumber <= this.defaultRetryNumber) {
                logger.error("RabbitMQ 失败记录: 当前消费者未配置重试, 直接拒绝消息, 关联ID={}, 配置重试次数={}",
                        correlationId, retryNumber);
            } else {
                logger.error("RabbitMQ 失败记录: 已达到重试上限, 删除幂等Key并拒绝消息, 关联ID={}, 已重试次数={}",
                        correlationId, actualRetry);
            }
            channel.basicNack(messageProperties.getDeliveryTag(), false, false);
        } else {
            logger.error("RabbitMQ 失败记录: 重试次数未达上限, 继续重试, 关联ID={}, 配置重试次数={}, 已重试次数={}",
                    correlationId, retryNumber, actualRetry);
            messageProperties.setHeader("retryNumber", actualRetry);
        }
    }

    /**
     * 发送请求-回应消息（RPC 场景）。
     *
     * <p>仅当消息携带了 {@code replyTo} 地址时才回发处理结果。</p>
     */
    public void sendReplyTo(R result) {
        if (ObjectUtil.isEmpty(messageProperties.getReplyTo())) {
            return;
        }
        ResultVO<R> resultVO = new ResultVO<>(result);
        // 构建响应消息，携带与请求一致的 correlationId 供消费端关联
        MessageProperties replyMessageProperties = new MessageProperties();
        replyMessageProperties.setCorrelationId(messageProperties.getCorrelationId());
        Message replyMessage = MessageBuilder.withBody(JSONUtil.toJsonStr(resultVO).getBytes())
                .andProperties(replyMessageProperties).build();
        rabbitUtils.sendReplyTo(messageProperties.getReplyTo(), replyMessage);
    }

    protected MessageIdempotencyExecutor messageIdempotencyExecutor() {
        if (cachedMessageIdempotencyExecutor != null) {
            return cachedMessageIdempotencyExecutor;
        }
        try {
            cachedMessageIdempotencyExecutor =
                    SpringUtil.getBeansOfType(MessageIdempotencyExecutor.class, null);
            return cachedMessageIdempotencyExecutor;
        } catch (Exception ex) {
            return null;
        }
    }

    protected String messageIdempotencyKey(RabbitMqModel<T> model) {
        if (model == null || StrUtil.isBlank(model.getMsgId())) {
            return null;
        }
        MessageListener listener = getClass().getAnnotation(MessageListener.class);
        String queue = listener == null || listener.queues().length == 0 ? "unknown" : listener.queues()[0];
        return String.join(":", "rabbitmq", queue, StrUtil.blankToDefault(tenantCode, "default"), model.getMsgId());
    }
}
