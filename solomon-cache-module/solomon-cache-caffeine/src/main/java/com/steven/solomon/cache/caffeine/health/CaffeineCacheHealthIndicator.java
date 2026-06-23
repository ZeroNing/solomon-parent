package com.steven.solomon.cache.caffeine.health;

import com.github.benmanes.caffeine.cache.Cache;
import com.steven.solomon.cache.caffeine.model.CaffeineCacheValue;
import com.steven.solomon.cache.caffeine.properties.CaffeineCacheProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class CaffeineCacheHealthIndicator implements HealthIndicator {

  private final Cache<String, CaffeineCacheValue> cache;
  private final CaffeineCacheProperties properties;

  public CaffeineCacheHealthIndicator(
      Cache<String, CaffeineCacheValue> cache,
      CaffeineCacheProperties properties) {
    this.cache = cache;
    this.properties = properties;
  }

  @Override
  public Health health() {
    return Health.up()
        .withDetail("component", "solomon-cache-caffeine")
        .withDetail("enabled", properties.isEnabled())
        .withDetail("estimatedSize", cache.estimatedSize())
        .withDetail("maximumSize", properties.getMaximumSize())
        .withDetail("defaultExpire", properties.getDefaultExpire().toString())
        .build();
  }
}
