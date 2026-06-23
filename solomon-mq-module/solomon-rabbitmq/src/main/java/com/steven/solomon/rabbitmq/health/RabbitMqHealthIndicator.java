package com.steven.solomon.rabbitmq.health;

import com.steven.solomon.rabbitmq.properties.RabbitMqProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class RabbitMqHealthIndicator implements HealthIndicator {

    private final ConnectionFactory connectionFactory;
    private final RabbitMqProperties properties;

    public RabbitMqHealthIndicator(ConnectionFactory connectionFactory, RabbitMqProperties properties) {
        this.connectionFactory = connectionFactory;
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (!properties.getEnabled()) {
            return Health.up()
                    .withDetail("enabled", false)
                    .withDetail("reason", "RabbitMQ is disabled")
                    .build();
        }
        try {
            Connection connection = connectionFactory.createConnection();
            if (connection != null) {
                connection.close();
            }
            return Health.up()
                    .withDetail("enabled", true)
                    .withDetail("connectionFactory", connectionFactory.getClass().getName())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("enabled", true)
                    .withDetail("connectionFactory", connectionFactory.getClass().getName())
                    .build();
        }
    }
}
