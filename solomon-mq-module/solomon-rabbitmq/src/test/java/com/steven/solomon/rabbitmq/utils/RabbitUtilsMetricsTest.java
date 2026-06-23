package com.steven.solomon.rabbitmq.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.steven.solomon.rabbitmq.entity.RabbitMqModel;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.MessagePostProcessor;

class RabbitUtilsMetricsTest {

    @Test
    void shouldRecordSendMetrics() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RabbitUtils rabbitUtils = new RabbitUtils(rabbitTemplate, true, meterRegistry);
        RabbitMqModel<String> message = new RabbitMqModel<>("exchange-a", "route-a", "payload");
        message.setTenantCode("tenant-a");
        message.setMsgId("msg-1");

        rabbitUtils.send(message);

        verify(rabbitTemplate).convertAndSend(
                eq("exchange-a"),
                eq("route-a"),
                eq(message),
                any(MessagePostProcessor.class),
                any(CorrelationData.class));
        assertThat(meterRegistry.counter(
                "solomon.mq.rabbitmq.send.total",
                "operation", "send",
                "exchange", "exchange-a",
                "routingKey", "route-a",
                "outcome", "success").count()).isEqualTo(1.0);
        assertThat(meterRegistry.timer(
                "solomon.mq.rabbitmq.send.duration",
                "operation", "send",
                "exchange", "exchange-a",
                "routingKey", "route-a",
                "outcome", "success").count()).isEqualTo(1);
    }
}
