package com.steven.solomon.mq.rocketmq;

import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class RocketMqHealthIndicator implements HealthIndicator {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMqHealthIndicator(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public Health health() {
        try {
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            if (producer == null) {
                return Health.down()
                        .withDetail("reason", "RocketMQ producer is not configured")
                        .build();
            }
            DefaultMQProducerImpl producerImpl = producer.getDefaultMQProducerImpl();
            if (producerImpl == null) {
                Health.Builder builder = Health.down()
                        .withDetail("reason", "RocketMQ producer implementation is not available");
                withProducerDetails(builder, producer);
                return builder.build();
            }
            ServiceState state = producerImpl.getServiceState();
            Health.Builder builder = ServiceState.RUNNING.equals(state) ? Health.up() : Health.down();
            withProducerDetails(builder, producer);
            return builder.withDetail("state", state == null ? "UNKNOWN" : state.name()).build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }

    private void withProducerDetails(Health.Builder builder, DefaultMQProducer producer) {
        if (producer.getProducerGroup() != null) {
            builder.withDetail("producerGroup", producer.getProducerGroup());
        }
        if (producer.getNamesrvAddr() != null) {
            builder.withDetail("namesrvAddr", producer.getNamesrvAddr());
        }
    }
}
