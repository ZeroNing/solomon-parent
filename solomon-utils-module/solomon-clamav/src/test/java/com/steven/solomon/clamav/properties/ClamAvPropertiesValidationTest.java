package com.steven.solomon.clamav.properties;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ClamAvPropertiesValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRequireHostAndPortWhenEnabled() {
    ClamAvProperties properties = new ClamAvProperties();
    properties.setEnabled(true);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("clamav.host and clamav.port"));
  }
}
