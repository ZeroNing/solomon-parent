package com.steven.solomon.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class NoticePropertiesValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRejectInvalidAsyncExecutorRange() {
    NoticeProperties properties = new NoticeProperties();
    properties.setAsyncQueueCapacity(0);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("async-queue-capacity"));
  }

  @Test
  void shouldRejectInvalidSendTimeout() {
    NoticeProperties properties = new NoticeProperties();
    properties.setSendTimeoutMillis(99);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("send-timeout-millis"));
  }

  @Test
  void shouldRejectInvalidRetryBackoff() {
    NoticeProperties properties = new NoticeProperties();
    properties.setRetryBackoffMillis(-1);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("retry-backoff-millis"));
  }

  @Test
  void shouldRejectPresentWebhookConfigWithoutUrl() {
    NoticeProperties properties = new NoticeProperties();
    properties.setDingTalk(new NoticeProperties.DingTalk());

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getPropertyPath().toString().equals("dingTalk.webhookUrl"));
  }
}
