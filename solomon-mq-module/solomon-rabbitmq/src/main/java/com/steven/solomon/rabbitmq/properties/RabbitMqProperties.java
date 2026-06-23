package com.steven.solomon.rabbitmq.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(
        prefix = "spring.rabbitmq"
)
@Validated
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

    @Valid
    @NotNull(message = "spring.rabbitmq.reliability must not be null")
    private Reliability reliability = new Reliability();

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

    public Reliability getReliability() {
        return reliability;
    }

    public void setReliability(Reliability reliability) {
        this.reliability = reliability;
    }

    public static class Reliability {

        private boolean requireDlxForRetry = false;

        public boolean isRequireDlxForRetry() {
            return requireDlxForRetry;
        }

        public void setRequireDlxForRetry(boolean requireDlxForRetry) {
            this.requireDlxForRetry = requireDlxForRetry;
        }
    }
}
