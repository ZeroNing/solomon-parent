package com.steven.solomon.config.annotation;

import org.apache.rocketmq.common.topic.TopicValidator;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RocketMq注解
 */
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RocketMqProducer {

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 生产者的名称
     */
    String producerGroup();

    /**
     * 实例名称，用于区分同一生产者组下不同实例
     */
    String instanceName();

    /**
     * 队列数量 默认4个
     */
    int defaultTopicQueueNums() default 4;

    /**
     * 当发送消息时，如果指定的Topic不存在，生产者会使用这个属性值作为Topic的名称来创建一个新的Topic
     */
    String createTopicKey() default TopicValidator.AUTO_CREATE_TOPIC_KEY_TOPIC;

    /**
     * 发送消息的超时时间，单位为毫秒。如果在指定时间内消息没有发送成功，则会抛出超时异常。
     */
    int sendMsgTimeout() default 3000;

    /**
     * 同步发送失败时的重试次数。如果消息发送失败，生产者会尝试重新发送消息，直到达到指定的重试次数
     */
    int retryTimesWhenSendFailed() default 2;

    /**
     * 异步发送失败时的重试次数。异步发送时，如果消息发送失败，生产者会尝试重新发送消息，直到达到指定的重试次数
     */
    int retryTimesWhenSendAsyncFailed() default 2;

    /**
     * 允许发送的最大消息大小，单位为字节。如果消息大小超过这个值，生产者将无法发送消息
     */
    int maxMessageSize() default  1024 * 1024 * 4;

    /**
     * 消息体超过指定大小后进行压缩，单位为字节
     */
    int compressMsgBodyOverHowmuch() default  1024 * 4;

    /**
     * 是否使用TLS（Transport Layer Security）进行安全通信。
     */
    boolean useTLS() default false;

    /**
     * 是否使用VIP通道发送消息。VIP通道是RocketMQ的一个优化选项，可以减少网络开销。
     */
    boolean sendMessageWithVIPChannel() default false;
}
