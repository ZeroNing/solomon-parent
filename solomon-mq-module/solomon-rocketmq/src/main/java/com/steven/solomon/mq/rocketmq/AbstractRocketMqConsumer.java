package com.steven.solomon.mq.rocketmq;

import com.steven.solomon.mq.AbstractMessageConsumer;
import com.steven.solomon.mq.model.BaseMq;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * RocketMQ 消费者基类。
 *
 * <p>继承 MQ 公共消费模板 {@link AbstractMessageConsumer}，统一处理消息反序列化、
 * 租户上下文绑定、幂等校验和消费日志。子类只需实现 {@link #handleMessage(Object)}。</p>
 *
 * <p>RocketMQ 的消息体约定为 {@link BaseMq} 的 JSON 序列化字符串，
 * 其中携带 {@code tenantCode} 字段用于多租户路由。</p>
 *
 * @param <T> 业务消息体类型
 * @param <R> 消费结果类型
 * @author steven
 */
public abstract class AbstractRocketMqConsumer<T, R>
    extends AbstractMessageConsumer<T, R, BaseMq<T>> {

    /**
     * 接收 RocketMQ 原始消息并交给公共消费模板处理。
     *
     * @param messageExt RocketMQ 消息对象
     */
    protected void onMessage(MessageExt messageExt) throws Exception {
        String topic = messageExt.getTopic();
        consumeMessage(topic, messageExt.getBody());
    }

    @Override
    protected Class<BaseMq<T>> messageModelType() {
        @SuppressWarnings("unchecked")
        Class<BaseMq<T>> type = (Class<BaseMq<T>>) (Class<?>) BaseMq.class;
        return type;
    }
}
