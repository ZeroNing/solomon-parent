package com.steven.solomon.properties;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "spring.rabbitmq"
)
public class RabbitMqProperties  {

    private boolean autoDeleteQueue = false;

    public boolean getAutoDeleteQueue() {
        return autoDeleteQueue;
    }

    public void setAutoDeleteQueue(boolean autoDeleteQueue) {
        this.autoDeleteQueue = autoDeleteQueue;
    }
}
