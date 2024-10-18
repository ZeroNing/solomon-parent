package com.steven.solomon.consumer;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.annotation.MessageListenerRetry;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.utils.RabbitUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * RabbitMq消费器
 */
public abstract class AbstractConsumer<T, R> extends MessageListenerAdapter {

    protected final Logger logger = LoggerUtils.logger(getClass());

    private final int defaultRetryNumber = 1;

    protected String correlationId;

    protected MessageProperties messageProperties;

    protected final RabbitUtils rabbitUtils;

    protected AbstractConsumer(RabbitUtils rabbitUtils) {
        this.rabbitUtils = rabbitUtils;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        setProperties(message);
        // 消费者内容
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        RabbitMqModel<T> rabbitMqModel = null;
        Throwable throwable = null;
        R result = null;
        try {
            logger.info("线程名:{},AbstractConsumer:消费者消息: {}", Thread.currentThread().getName(), json);
            rabbitMqModel = conversion(json);
            // 判断是否重复消费
            if (checkMessageKey(rabbitMqModel)) {
                throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
            }
            // 消费消息
            result = this.handleMessage(rabbitMqModel.getBody());
            if (!isAutoAck()) {
                // 手动确认消息
                channel.basicAck(messageProperties.getDeliveryTag(), false);
            }
            if(ValidateUtils.isNotEmpty(messageProperties.getReplyTo())){
                // 创建响应消息 Result必须是JSON结构才可以
                MessageProperties replyMessageProperties = new MessageProperties();
                replyMessageProperties.setCorrelationId(messageProperties.getCorrelationId());
                Message replyMessage = MessageBuilder.withBody(result.toString().getBytes()).andProperties(replyMessageProperties).build();
                rabbitUtils.sendReplyTo(messageProperties.getReplyTo(), replyMessage);
            }
        } catch (Throwable e) {
            // 保存重试失败次数达到retryNumber上线后拒绝此消息入队列并删除redis
            saveFailNumber(channel,e);
            throwable = e;
            throw e;
        } finally {
            // 删除判断重复消费Key
            deleteCheckMessageKey(rabbitMqModel);
            // 保存消费成功/失败的消息
            saveLog(result,throwable, rabbitMqModel);
        }
    }

    private void setProperties(Message message) {
        messageProperties = message.getMessageProperties();
        correlationId = messageProperties.getHeader("spring_listener_return_correlation");
    }

    /**
     * 记录失败次数并决定是否拒绝此消息
     */
    public void saveFailNumber(Channel channel,Throwable e) throws Exception {
        // 消费失败次数不等于空并且失败次数大于某个次数,不处理直接return,并记录到数据库
        logger.error("AbstractConsumer:消费报错 异常为:", e);

        Integer lock = messageProperties.getHeader("retryNumber");
        Integer actualLock = ValidateUtils.isEmpty(lock) ? 1 : lock + 1;
        int retryNumber = getRetryNumber();
        logger.error("rabbitMQ 失败记录:消费者correlationId为:{},deliveryTag为:{},失败次数为:{}", correlationId, messageProperties.getDeliveryTag(), actualLock);

        if (retryNumber <= this.defaultRetryNumber || actualLock >= retryNumber) {
            if (retryNumber <= this.defaultRetryNumber) {
                logger.error("rabbitMQ 失败记录:因记录不需要重试因此直接拒绝此消息,消费者correlationId为:{},消费者设置重试次数为:{}", correlationId, retryNumber);
            } else {
                logger.error("rabbitMQ 失败记录:已满足重试次数,删除redis消息并且拒绝此消息,消费者correlationId为:{},重试次数为:{}", correlationId, actualLock);
            }
            channel.basicNack(messageProperties.getDeliveryTag(), false, false);
        } else {
            logger.error("rabbitMQ 失败记录:因记录重试次数还未达到重试上限，还将继续进行重试,消费者correlationId为:{},消费者设置重试次数为:{},现重试次数为:{}", correlationId, retryNumber, actualLock);
            messageProperties.setHeader("retryNumber", actualLock);
        }
    }

    /**
     * 获取重试次数，默认为2
     */
    public int getRetryNumber() {
        MessageListenerRetry messageListenerRetry = getClass().getAnnotation(MessageListenerRetry.class);
        return ValidateUtils.isEmpty(messageListenerRetry) ? defaultRetryNumber : messageListenerRetry.retryNumber();
    }

    /**
     * 获取是否是自动确认
     */
    public boolean isAutoAck() {
        MessageListener messageListener = getClass().getAnnotation(MessageListener.class);
        return ValidateUtils.isNotEmpty(messageListener) && ValidateUtils.equalsIgnoreCase(AcknowledgeMode.AUTO.toString(), messageListener.mode().toString());
    }

    /**
     * 消费方法
     * @param body 请求数据
     */
    public abstract R handleMessage(T body) throws Exception;

    /**
     * 判断是否重复消费
     */
    public boolean checkMessageKey(RabbitMqModel<T> rabbitMqModel) {
        return false;
    }

    /**
     * 删除判断重复消费Key
     */
    public void deleteCheckMessageKey(RabbitMqModel<T> rabbitMqModel) {

    }

    /**
     * 保存消费成功/失败的消息
     */
    public abstract void saveLog(R result,Throwable throwable,RabbitMqModel<T> rabbitMqModel);

    /**
     * 转换消息
     */
    private RabbitMqModel<T> conversion(String json) {
        RabbitMqModel<T> model = JSONUtil.toBean(json, new TypeReference<RabbitMqModel<T>>() {},true);
        T body = model.getBody();
        boolean isJsonObject = body instanceof JSONObject;
        boolean isJsonArray = body instanceof JSONArray;
        if(!isJsonObject && !isJsonArray){
            return model;
        }
        Type typeArgument = TypeUtil.getTypeArgument(getClass(),0);
        body = JSONUtil.toBean(JSONUtil.toJsonStr(body),typeArgument,true);
        model.setBody(body);
        return model;
    }
}
