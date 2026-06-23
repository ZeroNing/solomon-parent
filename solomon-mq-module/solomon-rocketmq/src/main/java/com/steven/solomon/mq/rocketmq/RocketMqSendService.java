package com.steven.solomon.mq.rocketmq;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.mq.SendService;
import com.steven.solomon.mq.model.BaseMq;
import com.steven.solomon.utils.logger.LoggerUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.springframework.messaging.support.MessageBuilder;

public class RocketMqSendService<T> implements SendService<BaseMq<T>> {

    private static final Logger logger = LoggerUtils.logger(RocketMqSendService.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final MeterRegistry meterRegistry;

    public RocketMqSendService(RocketMQTemplate rocketMQTemplate) {
        this(rocketMQTemplate, null);
    }

    public RocketMqSendService(RocketMQTemplate rocketMQTemplate, MeterRegistry meterRegistry) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void send(BaseMq<T> data) throws Exception {
        String topic = resolveTopic(data);
        sendAndRecord("send", topic, data, 0, 0);
        logger.info("RocketMQ send succeeded, operation=send, topic={}, tenant={}, msgId={}",
                topic, data.getTenantCode(), data.getMsgId());
    }

    @Override
    public void sendDelay(BaseMq<T> data, long delay) throws Exception {
        String topic = resolveTopic(data);
        int delayLevel = calcDelayLevel(delay);
        sendAndRecord("delay", topic, data, delayLevel, 0);
        logger.info("RocketMQ send succeeded, operation=delay, topic={}, tenant={}, msgId={}, delayLevel={}",
                topic, data.getTenantCode(), data.getMsgId(), delayLevel);
    }

    @Override
    public void sendExpiration(BaseMq<T> data, long expiration) throws Exception {
        String topic = resolveTopic(data);
        int delayLevel = calcDelayLevel(expiration);
        sendAndRecord("expiration", topic, data, delayLevel, 0);
        logger.info("RocketMQ send succeeded, operation=expiration, topic={}, tenant={}, msgId={}, expirationMillis={}",
                topic, data.getTenantCode(), data.getMsgId(), expiration);
    }

    protected String resolveTopic(BaseMq<T> data) {
        return data.getClass().getSimpleName();
    }

    private void sendAndRecord(String operation, String topic, BaseMq<T> data, int delayLevel, long timeout) {
        Timer.Sample sample = meterRegistry == null ? null : Timer.start(meterRegistry);
        String outcome = "success";
        try {
            doSend(topic, data, delayLevel, timeout);
        } catch (RuntimeException e) {
            outcome = "error";
            logger.error("RocketMQ send failed, operation={}, topic={}, tenant={}, msgId={}",
                    operation, topic, data.getTenantCode(), data.getMsgId(), e);
            throw e;
        } finally {
            recordMetrics(operation, topic, outcome, sample);
        }
    }

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

    private void recordMetrics(String operation, String topic, String outcome, Timer.Sample sample) {
        if (meterRegistry == null) {
            return;
        }
        Tags tags = Tags.of("operation", operation, "topic", topic, "outcome", outcome);
        meterRegistry.counter("solomon.mq.rocketmq.send.total", tags).increment();
        if (sample != null) {
            sample.stop(Timer.builder("solomon.mq.rocketmq.send.duration").tags(tags).register(meterRegistry));
        }
    }

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
