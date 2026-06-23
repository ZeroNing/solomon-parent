package com.steven.solomon.rabbitmq.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.steven.solomon.rabbitmq.annotation.MessageListener;
import com.steven.solomon.rabbitmq.annotation.MessageListenerRetry;
import com.steven.solomon.rabbitmq.consumer.AbstractConsumer;
import com.steven.solomon.rabbitmq.entity.RabbitMqModel;
import com.steven.solomon.rabbitmq.properties.RabbitMqProperties;
import com.steven.solomon.rabbitmq.utils.RabbitUtils;
import org.junit.jupiter.api.Test;

class RabbitRetryTopologyValidatorTest {

  @Test
  void shouldFailWhenRetryRequiresDlxButListenerHasNoDlx() {
    RabbitMqProperties properties = new RabbitMqProperties();
    properties.getReliability().setRequireDlxForRetry(true);

    assertThatThrownBy(() -> RabbitRetryTopologyValidator.validate(
        properties,
        NoDlxRetryConsumer.class.getAnnotation(MessageListener.class),
        NoDlxRetryConsumer.class.getAnnotation(MessageListenerRetry.class),
        NoDlxRetryConsumer.class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("require-dlx-for-retry=true");
  }

  @Test
  void shouldPassWhenRetryHasDlxConsumer() {
    RabbitMqProperties properties = new RabbitMqProperties();
    properties.getReliability().setRequireDlxForRetry(true);

    assertThatCode(() -> RabbitRetryTopologyValidator.validate(
        properties,
        WithDlxRetryConsumer.class.getAnnotation(MessageListener.class),
        WithDlxRetryConsumer.class.getAnnotation(MessageListenerRetry.class),
        WithDlxRetryConsumer.class))
        .doesNotThrowAnyException();
  }

  @MessageListener(queues = "queue-a")
  @MessageListenerRetry(retryNumber = 2, initialInterval = 1, maxInterval = 2, multiplier = 1)
  private static class NoDlxRetryConsumer extends TestConsumer {
    NoDlxRetryConsumer(RabbitUtils rabbitUtils) {
      super(rabbitUtils);
    }
  }

  @MessageListener(queues = "queue-a", dlxClazz = DlxConsumer.class)
  @MessageListenerRetry(retryNumber = 2, initialInterval = 1, maxInterval = 2, multiplier = 1)
  private static class WithDlxRetryConsumer extends TestConsumer {
    WithDlxRetryConsumer(RabbitUtils rabbitUtils) {
      super(rabbitUtils);
    }
  }

  private static class DlxConsumer extends TestConsumer {
    DlxConsumer(RabbitUtils rabbitUtils) {
      super(rabbitUtils);
    }
  }

  private abstract static class TestConsumer extends AbstractConsumer<String, String> {

    TestConsumer(RabbitUtils rabbitUtils) {
      super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) {
      return body;
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> model) {
    }
  }
}
