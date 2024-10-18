package com.steven.solomon.properties;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "spring.rabbitmq.customized"
)
public class RabbitMqProperties  {

    private boolean deleteQueue = false;

    public boolean getDeleteQueue() {
        return deleteQueue;
    }

    public void setDeleteQueue(boolean deleteQueue) {
        this.deleteQueue = deleteQueue;
    }
}
