package com.steven.solomon.mq.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RocketMqHealthIndicatorTest {

    @Test
    void shouldReportUpWhenProducerIsRunning() {
        RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
        DefaultMQProducer producer = producerWithState(ServiceState.RUNNING);
        producer.setProducerGroup("group-a");
        producer.setNamesrvAddr("127.0.0.1:9876");
        when(rocketMQTemplate.getProducer()).thenReturn(producer);

        Health health = new RocketMqHealthIndicator(rocketMQTemplate).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("producerGroup", "group-a");
        assertThat(health.getDetails()).containsEntry("namesrvAddr", "127.0.0.1:9876");
        assertThat(health.getDetails()).containsEntry("state", "RUNNING");
    }

    @Test
    void shouldReportDownWhenProducerIsNotRunning() {
        RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
        DefaultMQProducer producer = producerWithState(ServiceState.START_FAILED);
        when(rocketMQTemplate.getProducer()).thenReturn(producer);

        Health health = new RocketMqHealthIndicator(rocketMQTemplate).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("state", "START_FAILED");
    }

    @Test
    void shouldReportDownWhenProducerIsMissing() {
        RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
        when(rocketMQTemplate.getProducer()).thenReturn(null);

        Health health = new RocketMqHealthIndicator(rocketMQTemplate).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "RocketMQ producer is not configured");
    }

    private DefaultMQProducer producerWithState(ServiceState state) {
        DefaultMQProducer producer = new DefaultMQProducer("health-test");
        producer.getDefaultMQProducerImpl().setServiceState(state);
        return producer;
    }
}
