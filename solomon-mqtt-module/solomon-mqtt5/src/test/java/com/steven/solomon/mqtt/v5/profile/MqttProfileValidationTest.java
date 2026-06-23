package com.steven.solomon.mqtt.v5.profile;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MqttProfileValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRejectInvalidMqtt5LimitsAndUserProperties() {
    MqttProfile profile = new MqttProfile();
    profile.setUrl("tcp://localhost:1883");
    profile.setClientId("client-v5");
    profile.setReceiveMaximum(0);
    profile.setUserProperties(Map.of("trace", ""));

    Set<String> messages = validator.validate(profile).stream()
        .map(violation -> violation.getMessage())
        .collect(java.util.stream.Collectors.toSet());

    assertThat(messages)
        .contains("mqtt.tenant[].receive-maximum must be at least 1")
        .contains("mqtt.tenant[].user-properties value must not be blank");
  }
}
