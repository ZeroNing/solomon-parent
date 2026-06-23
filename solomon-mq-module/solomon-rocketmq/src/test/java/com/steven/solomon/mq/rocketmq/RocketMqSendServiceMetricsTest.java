package com.steven.solomon.mq.rocketmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.steven.solomon.mq.model.BaseMq;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

class RocketMqSendServiceMetricsTest {

    @Test
    void shouldRecordSendMetrics() throws Exception {
        RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RocketMqSendService<String> service = new RocketMqSendService<>(rocketMQTemplate, meterRegistry);
        BaseMq<String> message = new BaseMq<>("payload");
        message.setMsgId("msg-1");
        message.setTenantCode("tenant-a");

        service.send(message);

        verify(rocketMQTemplate).syncSend(eq("BaseMq"), any(Message.class), eq(0L));
        assertThat(meterRegistry.counter(
                "solomon.mq.rocketmq.send.total",
                "operation", "send",
                "topic", "BaseMq",
                "outcome", "success").count()).isEqualTo(1.0);
        assertThat(meterRegistry.timer(
                "solomon.mq.rocketmq.send.duration",
                "operation", "send",
                "topic", "BaseMq",
                "outcome", "success").count()).isEqualTo(1);
    }
}
