package com.steven.solomon.rabbitmq.health;

import com.steven.solomon.rabbitmq.properties.RabbitMqProperties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RabbitMqHealthIndicatorTest {

    @Test
    void shouldReportUpWithoutConnectingWhenDisabled() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        RabbitMqProperties properties = new RabbitMqProperties();
        properties.setEnabled(false);

        Health health = new RabbitMqHealthIndicator(connectionFactory, properties).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("enabled", false);
        verify(connectionFactory, never()).createConnection();
    }

    @Test
    void shouldReportUpWhenConnectionCanBeOpened() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        Connection connection = mock(Connection.class);
        RabbitMqProperties properties = new RabbitMqProperties();
        when(connectionFactory.createConnection()).thenReturn(connection);

        Health health = new RabbitMqHealthIndicator(connectionFactory, properties).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("enabled", true);
        verify(connection).close();
    }

    @Test
    void shouldReportDownWhenConnectionFails() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        RabbitMqProperties properties = new RabbitMqProperties();
        when(connectionFactory.createConnection()).thenThrow(new AmqpConnectException(new RuntimeException("down")));

        Health health = new RabbitMqHealthIndicator(connectionFactory, properties).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("enabled", true);
        assertThat(health.getDetails()).containsKey("error");
    }
}
