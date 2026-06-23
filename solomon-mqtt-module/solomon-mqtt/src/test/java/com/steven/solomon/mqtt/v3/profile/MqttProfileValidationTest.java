package com.steven.solomon.mqtt.v3.profile;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MqttProfileValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRejectMissingRequiredConnectionFields() {
    MqttProfile profile = new MqttProfile();

    Set<String> messages = validator.validate(profile).stream()
        .map(violation -> violation.getMessage())
        .collect(java.util.stream.Collectors.toSet());

    assertThat(messages)
        .contains("mqtt.tenant[].url must not be blank")
        .contains("mqtt.tenant[].client-id must not be blank");
  }

  @Test
  void shouldRejectInvalidWillQos() {
    MqttProfile profile = validProfile();
    MqttProfile.MqttWill will = new MqttProfile.MqttWill();
    will.setTopic("offline");
    will.setMessage("bye");
    will.setQos(3);
    profile.setWill(will);

    Set<String> messages = validator.validate(profile).stream()
        .map(violation -> violation.getMessage())
        .collect(java.util.stream.Collectors.toSet());

    assertThat(messages).contains("mqtt.tenant[].will.qos must be between 0 and 2");
  }

  private MqttProfile validProfile() {
    MqttProfile profile = new MqttProfile();
    profile.setUrl("tcp://localhost:1883");
    profile.setClientId("client-a");
    return profile;
  }
}
