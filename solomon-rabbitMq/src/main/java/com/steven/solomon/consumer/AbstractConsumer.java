package com.steven.solomon.consumer;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.steven.solomon.annotation.RabbitMq;
import com.steven.solomon.annotation.RabbitMqRetry;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMq消费器
 */
public abstract class AbstractConsumer<T, R> extends MessageListenerAdapter {

    protected final Logger logger = LoggerUtils.logger(getClass());

    private final int defaultRetryNumber = 1;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        String correlationId = messageProperties.getHeader("spring_listener_return_correlation");
        try {
            // 消费者内容
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            logger.debug("线程名:{},AbstractConsumer:消费者消息: {}", Thread.currentThread().getName(), json);
            RabbitMqModel<T> rabbitMqModel = JSONUtil.toBean(json, new TypeReference<RabbitMqModel<T>>(){},true);
            // 判断是否重复消费
            if (checkMessageKey(messageProperties,rabbitMqModel)) {
                throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
            }
            // 消费消息
            R result = this.handleMessage(rabbitMqModel.getBody());
            if (!isAutoAck()) {
                // 手动确认消息
                channel.basicAck(deliveryTag, false);
            }
            // 保存消费成功消息
            saveLog(result, message, rabbitMqModel);
        } catch (Throwable e) {
            // 消费失败次数不等于空并且失败次数大于某个次数,不处理直接return,并记录到数据库
            logger.error("AbstractConsumer:消费报错 异常为:", e);
            // 将消费失败的记录保存到数据库或者不处理也可以
            this.saveFailMessage(message, e);
            // 保存重试失败次数达到retryNumber上线后拒绝此消息入队列并删除redis
            saveFailNumber(messageProperties, channel, deliveryTag, correlationId);
            throw e;
        } finally {
            // 删除判断重复消费Key
            deleteCheckMessageKey(messageProperties);
        }
    }

    /**
     * 记录失败次数并决定是否拒绝此消息
     */
    public void saveFailNumber(MessageProperties messageProperties, Channel channel, long deliveryTag, String correlationId) throws Exception {
        Integer lock = messageProperties.getHeader("retryNumber");
        Integer actualLock = ValidateUtils.isEmpty(lock) ? 1 : lock + 1;
        logger.error("rabbitMQ 失败记录:消费者correlationId为:{},deliveryTag为:{},失败次数为:{}", correlationId, deliveryTag, actualLock);
        int retryNumber = getRetryNumber();
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
        RabbitMqRetry rabbitMqRetry = getClass().getAnnotation(RabbitMqRetry.class);
        return ValidateUtils.isEmpty(rabbitMqRetry) ? defaultRetryNumber : rabbitMqRetry.retryNumber();
    }

    /**
     * 获取是否是自动确认
     */
    public boolean isAutoAck() {
        RabbitMq rabbitMq = getClass().getAnnotation(RabbitMq.class);
        return ValidateUtils.isNotEmpty(rabbitMq) && ValidateUtils.equalsIgnoreCase(AcknowledgeMode.AUTO.toString(), rabbitMq.mode().toString());
    }

    /**
     * 消费方法
     * @param body 请求数据
     */
    public abstract R handleMessage(T body) throws Exception;

    /**
     * 保存消费失败的消息
     *
     * @param message mq所包含的信息
     * @param e       异常
     */
    public void saveFailMessage(Message message, Throwable e) {

    }

    /**
     * 判断是否重复消费
     */
    public boolean checkMessageKey(MessageProperties messageProperties,RabbitMqModel<T> rabbitMqModel) {
        return false;
    }

    /**
     * 删除判断重复消费Key
     */
    public void deleteCheckMessageKey(MessageProperties messageProperties) {

    }

    /**
     * 保存消费成功消息
     */
    public abstract void saveLog(R result, Message message, RabbitMqModel<T> rabbitMqModel);

}
