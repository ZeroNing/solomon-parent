package com.steven.solomon.mqtt.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.steven.solomon.holder.RequestHeaderHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MqttMessageModelTest {

  @AfterEach
  void tearDown() {
    RequestHeaderHolder.remove();
  }

  @Test
  void inheritTenantCodeFromCurrentContext() {
    RequestHeaderHolder.setTenantCode("tenant-1");
    MqttMessageModel<String> message = new MqttMessageModel<>();

    message.inheritTenantCode();

    assertEquals("tenant-1", message.getTenantCode());
  }

  @Test
  void keepExplicitTenantCode() {
    RequestHeaderHolder.setTenantCode("tenant-1");
    MqttMessageModel<String> message = new MqttMessageModel<>("tenant-2");

    message.inheritTenantCode();

    assertEquals("tenant-2", message.getTenantCode());
  }
}
