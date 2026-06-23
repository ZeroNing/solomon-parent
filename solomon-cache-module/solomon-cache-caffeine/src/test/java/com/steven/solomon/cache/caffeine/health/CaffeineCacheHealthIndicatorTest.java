package com.steven.solomon.cache.caffeine.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.steven.solomon.cache.caffeine.model.CaffeineCacheValue;
import com.steven.solomon.cache.caffeine.properties.CaffeineCacheProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class CaffeineCacheHealthIndicatorTest {

  @Test
  void shouldReportCacheDetails() {
    CaffeineCacheProperties properties = new CaffeineCacheProperties();
    properties.setEnabled(true);
    properties.setMaximumSize(128);
    properties.setDefaultExpire(Duration.ofMinutes(5));
    com.github.benmanes.caffeine.cache.Cache<String, CaffeineCacheValue> cache =
        Caffeine.newBuilder().build();
    cache.put("key", new CaffeineCacheValue("value", 0));

    Health health = new CaffeineCacheHealthIndicator(cache, properties).health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsEntry("component", "solomon-cache-caffeine");
    assertThat(health.getDetails()).containsEntry("enabled", true);
    assertThat(health.getDetails()).containsEntry("maximumSize", 128L);
    assertThat(health.getDetails()).containsEntry("estimatedSize", 1L);
  }
}
