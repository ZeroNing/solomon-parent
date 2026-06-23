package com.steven.solomon.properties;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.enums.FileChoiceEnum;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class FileChoicePropertiesValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldAllowDefaultProviderWithoutCredentials() {
    FileChoiceProperties properties = new FileChoiceProperties();

    assertThat(validator.validate(properties)).isEmpty();
  }

  @Test
  void shouldRequireProviderCredentialsWhenProviderSelected() {
    FileChoiceProperties properties = new FileChoiceProperties();
    properties.setChoice(FileChoiceEnum.MINIO);

    assertThat(validator.validate(properties))
        .anyMatch(violation -> violation.getMessage().contains("file.endpoint"));
  }
}
