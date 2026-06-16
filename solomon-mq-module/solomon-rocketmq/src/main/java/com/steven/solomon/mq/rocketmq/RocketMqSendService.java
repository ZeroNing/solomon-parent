package com.steven.solomon.mq.rocketmq;

import cn.hutool.json.JSONUtil;

import com.steven.solomon.mq.SendService;
import com.steven.solomon.mq.model.BaseMq;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.springframework.messaging.support.MessageBuilder;

/**
 * RocketMQ 消息发送服务实现。
 *
 * <p>基于 {@link RocketMQTemplate} 实现即时发送、延时发送和过期发送。
 * 消息体统一封装为 {@link BaseMq}，租户编码通过消息头 {@code tenantCode} 透传，
 * 消费端据此完成多租户路由。</p>
 *
 * @param <T> 业务消息体类型
 * @author steven
 */
public class RocketMqSendService<T> implements SendService<BaseMq<T>> {

    private static final Logger logger = LoggerUtils.logger(RocketMqSendService.class);

    /** RocketMQ 发送模板，由 Spring 容器注入。 */
    private final RocketMQTemplate rocketMQTemplate;

    public RocketMqSendService(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void send(BaseMq<T> data) throws Exception {
        String topic = resolveTopic(data);
        doSend(topic, data, 0, 0);
        logger.info("RocketMQ 即时发送成功, topic={}, 租户={}, 消息ID={}",
                topic, data.getTenantCode(), data.getMsgId());
    }

    @Override
    public void sendDelay(BaseMq<T> data, long delay) throws Exception {
        String topic = resolveTopic(data);
        // RocketMQ 延时级别（1-18），这里按毫秒就近映射到 delayLevel
        int delayLevel = calcDelayLevel(delay);
        doSend(topic, data, delayLevel, 0);
        logger.info("RocketMQ 延时发送成功, topic={}, 租户={}, 延时级别={}",
                topic, data.getTenantCode(), delayLevel);
    }

    @Override
    public void sendExpiration(BaseMq<T> data, long expiration) throws Exception {
        // RocketMQ 没有原生过期丢弃能力，这里用延时模拟：过期时间到达后投递
        String topic = resolveTopic(data);
        int delayLevel = calcDelayLevel(expiration);
        doSend(topic, data, delayLevel, 0);
        logger.info("RocketMQ 过期发送成功(按延时模拟), topic={}, 租户={}, 过期时间={}ms",
                topic, data.getTenantCode(), expiration);
    }

    /**
     * 执行实际发送，租户编码写入消息头供消费端路由。
     */
    private void doSend(String topic, BaseMq<T> data, int delayLevel, long timeout) {
        MessageBuilder<?> builder = MessageBuilder.withPayload(JSONUtil.toJsonStr(data))
                .setHeader(RocketMQHeaders.KEYS, data.getMsgId());
        if (data.getTenantCode() != null) {
            builder.setHeader("tenantCode", data.getTenantCode());
        }
        if (delayLevel > 0) {
            rocketMQTemplate.syncSend(topic, builder.build(), timeout, delayLevel);
        } else {
            rocketMQTemplate.syncSend(topic, builder.build(), timeout);
        }
    }

    /**
     * 根据消息体推断目标 topic，默认使用消息模型中的 topic 字段或类名。
     * 业务可重写以自定义 topic 命名规则。
     */
    protected String resolveTopic(BaseMq<T> data) {
        return data.getClass().getSimpleName();
    }

    /**
     * 将毫秒延时映射到 RocketMQ 的延时级别（18 个固定级别）。
     * 级别说明：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h。
     */
    private int calcDelayLevel(long delayMillis) {
        long seconds = delayMillis / 1000;
        if (seconds < 5) return 1;
        if (seconds < 10) return 2;
        if (seconds < 30) return 3;
        if (seconds < 60) return 4;
        if (seconds < 120) return 5;
        if (seconds < 180) return 6;
        if (seconds < 300) return 7;
        if (seconds < 600) return 8;
        if (seconds < 1200) return 9;
        if (seconds < 1800) return 10;
        if (seconds < 3600) return 11;
        if (seconds < 7200) return 12;
        return 14;
    }
}
