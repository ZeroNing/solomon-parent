package com.steven.solomon.properties;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "spring.rabbitmq"
)
public class RabbitMqProperties  {
    /**
     * 是否自动删除队列
     */
    private boolean autoDeleteQueue = false;

    /**
     * 是否自动删除交换机
     */
    private boolean autoDeleteExchange = false;

    //是否启用
    private boolean enabled = true;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getAutoDeleteExchange() {
        return autoDeleteExchange;
    }

    public void setAutoDeleteExchange(boolean autoDeleteExchange) {
        this.autoDeleteExchange = autoDeleteExchange;
    }

    public boolean getAutoDeleteQueue() {
        return autoDeleteQueue;
    }

    public void setAutoDeleteQueue(boolean autoDeleteQueue) {
        this.autoDeleteQueue = autoDeleteQueue;
    }
}
