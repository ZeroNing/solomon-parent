package com.steven.solomon.job.xxl.properties;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class XxlJobPropertiesValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRequireAdminAddressAndAppNameWhenEnabled() {
    XxlJobProperties properties = new XxlJobProperties();

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("xxl.admin-addresses"));
  }

  @Test
  void shouldRequireCredentialsWhenRegisterEnabled() {
    XxlJobRegisterProperties properties = new XxlJobRegisterProperties();
    properties.setEnabled(true);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("xxl.register.user-name"));
  }

  @Test
  void shouldRejectInvalidAdminApiRetryConfig() {
    XxlJobRegisterProperties properties = new XxlJobRegisterProperties();
    properties.setAdminApiMaxAttempts(0);
    properties.setAdminApiBackoffMillis(-1);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("xxl.register.admin-api-max-attempts"))
        .anyMatch(violation -> violation.getMessage().contains("xxl.register.admin-api-backoff-millis"));
  }
}
