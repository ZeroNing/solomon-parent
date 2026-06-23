package com.steven.solomon.mqtt.vertx.profile;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MqttProfileValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldRejectInvalidVertxThreadPoolConfiguration() {
    MqttProfile profile = new MqttProfile();
    profile.setUrl("tcp://localhost:1883");
    profile.setClientId("client-vertx");
    MqttProfile.VertxConfig vertxConfig = new MqttProfile.VertxConfig();
    vertxConfig.setWorkerPoolSize(0);
    profile.setVertx(vertxConfig);

    Set<String> messages = validator.validate(profile).stream()
        .map(violation -> violation.getMessage())
        .collect(java.util.stream.Collectors.toSet());

    assertThat(messages).contains("mqtt.tenant[].vertx.worker-pool-size must be at least 1");
  }
}
