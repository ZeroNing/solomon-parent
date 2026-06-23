package com.steven.solomon.cache.redis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.key.CacheKeyProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisCacheServiceMetricsTest {

  @Test
  void shouldRecordSuccessfulOperationMetrics() {
    RedisTemplate<String, Object> redisTemplate = mockRedisTemplate();
    when(redisTemplate.opsForValue().get("key")).thenReturn("value");
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    RedisCacheService service = new RedisCacheService(
        redisTemplate, new CacheKeyBuilder(new CacheKeyProperties()), meterRegistry);

    String value = service.get("group", "key");

    assertThat(value).isEqualTo("value");

    assertThat(meterRegistry.counter("solomon.cache.redis.operation.total",
        "operation", "get", "group", "group", "outcome", "success").count()).isEqualTo(1);
    assertThat(meterRegistry.find("solomon.cache.redis.operation.duration").timer()).isNotNull();
  }

  @Test
  void shouldRecordFailedOperationMetrics() {
    RedisTemplate<String, Object> redisTemplate = mockRedisTemplate();
    when(redisTemplate.opsForValue().get("key")).thenThrow(new IllegalStateException("boom"));
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    RedisCacheService service = new RedisCacheService(
        redisTemplate, new CacheKeyBuilder(new CacheKeyProperties()), meterRegistry);

    assertThatThrownBy(() -> service.get("group", "key"))
        .isInstanceOf(IllegalStateException.class);

    assertThat(meterRegistry.counter("solomon.cache.redis.operation.total",
        "operation", "get", "group", "group", "outcome", "error").count()).isEqualTo(1);
  }

  @SuppressWarnings("unchecked")
  private RedisTemplate<String, Object> mockRedisTemplate() {
    RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    return redisTemplate;
  }
}
