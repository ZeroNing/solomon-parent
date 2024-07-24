package com.steven.solomon.profile;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "spring.redis")
public class TenantRedisProperties {

  private Map<String,RedisProperties> tenant;

  public Map<String, RedisProperties> getTenant() {
    return tenant;
  }

  public void setTenant(
      Map<String, RedisProperties> tenant) {
    this.tenant = tenant;
  }
}
