package com.steven.solomon.profile;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
