package com.steven.solomon.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("mqtt")
public class TenantMqttProfile {

  public Map<String,MqttProfile> tenant;

  public Map<String, MqttProfile> getTenant() {
    return tenant;
  }

  public void setTenant(Map<String, MqttProfile> tenant) {
    this.tenant = tenant;
  }
}
