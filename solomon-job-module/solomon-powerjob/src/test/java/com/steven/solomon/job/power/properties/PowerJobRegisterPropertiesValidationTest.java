package com.steven.solomon.job.power.properties;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class PowerJobRegisterPropertiesValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRequireLoginAndNamespaceWhenRegisterEnabled() {
    PowerJobRegisterProperties properties = new PowerJobRegisterProperties();
    properties.setEnabled(true);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("powerjob.worker.register.user-name"));
  }

  @Test
  void shouldRejectNullModeAndFailureStrategy() {
    PowerJobRegisterProperties properties = new PowerJobRegisterProperties();
    properties.setMode(null);
    properties.setFailureStrategy(null);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("powerjob.worker.register.mode"))
        .anyMatch(violation -> violation.getMessage().contains("powerjob.worker.register.failure-strategy"));
  }

  @Test
  void shouldRejectInvalidAdminApiRetryConfig() {
    PowerJobRegisterProperties properties = new PowerJobRegisterProperties();
    properties.setAdminApiMaxAttempts(0);
    properties.setAdminApiBackoffMillis(-1);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("powerjob.worker.register.admin-api-max-attempts"))
        .anyMatch(violation -> violation.getMessage().contains("powerjob.worker.register.admin-api-backoff-millis"));
  }
}
