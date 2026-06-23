package com.steven.solomon.context;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class TenantModePropertiesValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRejectBlankDefaultTenantCode() {
    TenantModeProperties properties = new TenantModeProperties();
    properties.setDefaultCode(" ");

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("tenant.default-code"));
  }
}
