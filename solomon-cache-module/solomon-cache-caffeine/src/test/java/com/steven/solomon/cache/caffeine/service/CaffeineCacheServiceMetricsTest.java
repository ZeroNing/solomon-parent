package com.steven.solomon.cache.caffeine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.steven.solomon.cache.caffeine.model.CaffeineCacheValue;
import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.key.CacheKeyProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class CaffeineCacheServiceMetricsTest {

  @Test
  void shouldRecordSuccessfulOperationMetrics() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    CaffeineCacheService service = new CaffeineCacheService(
        Caffeine.newBuilder().build(),
        new CacheKeyBuilder(new CacheKeyProperties()),
        60,
        meterRegistry);

    service.set("group", "key", "value");
    String value = service.get("group", "key");

    assertThat(value).isEqualTo("value");
    assertThat(meterRegistry.counter("solomon.cache.caffeine.operation.total",
        "operation", "get", "group", "group", "outcome", "success").count()).isEqualTo(1);
    assertThat(meterRegistry.find("solomon.cache.caffeine.operation.duration").timer()).isNotNull();
  }

  @Test
  void shouldRecordFailedOperationMetrics() {
    Cache<String, CaffeineCacheValue> cache = mockCache();
    when(cache.getIfPresent("key")).thenThrow(new IllegalStateException("boom"));
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    CaffeineCacheService service = new CaffeineCacheService(
        cache,
        new CacheKeyBuilder(new CacheKeyProperties()),
        60,
        meterRegistry);

    assertThatThrownBy(() -> service.get("group", "key"))
        .isInstanceOf(IllegalStateException.class);
    assertThat(meterRegistry.counter("solomon.cache.caffeine.operation.total",
        "operation", "get", "group", "group", "outcome", "error").count()).isEqualTo(1);
  }

  @SuppressWarnings("unchecked")
  private Cache<String, CaffeineCacheValue> mockCache() {
    return mock(Cache.class);
  }
}
