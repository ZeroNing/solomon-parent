package com.steven.solomon.rabbitmq.config;

import com.steven.solomon.rabbitmq.annotation.MessageListener;
import com.steven.solomon.rabbitmq.annotation.MessageListenerRetry;
import com.steven.solomon.rabbitmq.properties.RabbitMqProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RabbitRetryTopologyValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitRetryTopologyValidator.class);

  private RabbitRetryTopologyValidator() {
  }

  public static void validate(
      RabbitMqProperties properties,
      MessageListener listener,
      MessageListenerRetry retry,
      Class<?> consumerType) {
    if (retry == null) {
      return;
    }
    if (retry.retryNumber() < 1) {
      throw new IllegalArgumentException("RabbitMQ retryNumber must be at least 1: " + consumerType.getName());
    }
    if (retry.initialInterval() < 0) {
      throw new IllegalArgumentException("RabbitMQ initialInterval must not be negative: " + consumerType.getName());
    }
    if (retry.maxInterval() < retry.initialInterval()) {
      throw new IllegalArgumentException("RabbitMQ maxInterval must be greater than or equal to initialInterval: "
          + consumerType.getName());
    }
    if (retry.multiplier() < 1) {
      throw new IllegalArgumentException("RabbitMQ multiplier must be at least 1: " + consumerType.getName());
    }
    boolean hasDlxConsumer = listener != null && !void.class.equals(listener.dlxClazz());
    boolean requireDlx = properties != null
        && properties.getReliability() != null
        && properties.getReliability().isRequireDlxForRetry();
    if (requireDlx && !hasDlxConsumer) {
      throw new IllegalArgumentException(
          "RabbitMQ retry listener must configure MessageListener.dlxClazz when spring.rabbitmq.reliability.require-dlx-for-retry=true: "
              + consumerType.getName());
    }
    if (!hasDlxConsumer) {
      LOGGER.warn("RabbitMQ retry listener has no dlxClazz, consumer={}", consumerType.getName());
    }
  }
}
