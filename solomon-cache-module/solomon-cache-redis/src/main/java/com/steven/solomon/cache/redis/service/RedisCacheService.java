package com.steven.solomon.cache.redis.service;

import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.metrics.CacheMetricsRecorder;
import com.steven.solomon.cache.service.CacheService;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

public class RedisCacheService implements CacheService {

  private static final int SCAN_COUNT = 1000;
  private static final int DELETE_BATCH_SIZE = 500;
  private static final String METRIC_TOTAL = "solomon.cache.redis.operation.total";
  private static final String METRIC_DURATION = "solomon.cache.redis.operation.duration";
  private static final String METRIC_DURATION_DESCRIPTION = "Redis cache operation duration";

  private final RedisTemplate<String, Object> redisTemplate;
  private final CacheKeyBuilder keyBuilder;
  private final CacheMetricsRecorder metricsRecorder;

  public RedisCacheService(RedisTemplate<String, Object> redisTemplate, CacheKeyBuilder keyBuilder) {
    this(redisTemplate, keyBuilder, null);
  }

  public RedisCacheService(
      RedisTemplate<String, Object> redisTemplate,
      CacheKeyBuilder keyBuilder,
      MeterRegistry meterRegistry) {
    this.redisTemplate = redisTemplate;
    this.keyBuilder = keyBuilder;
    this.metricsRecorder = new CacheMetricsRecorder(
        meterRegistry, METRIC_TOTAL, METRIC_DURATION, METRIC_DURATION_DESCRIPTION);
  }

  @Override
  public void expire(String group, String key, long seconds) {
    metricsRecorder.record("expire", group,
        () -> redisTemplate.expire(buildKey(group, key), seconds, TimeUnit.SECONDS));
  }

  @Override
  public Long getExpire(String group, String key) {
    return metricsRecorder.record("getExpire", group,
        () -> redisTemplate.getExpire(buildKey(group, key), TimeUnit.SECONDS));
  }

  @Override
  public Boolean hasKey(String group, String key) {
    return metricsRecorder.record("hasKey", group, () -> redisTemplate.hasKey(buildKey(group, key)));
  }

  @Override
  public void delete(String group, String... keys) {
    metricsRecorder.record("delete", group, () -> {
      if (keys == null || keys.length == 0) {
        return null;
      }
      redisTemplate.delete(Arrays.stream(keys)
          .filter(Objects::nonNull)
          .map(key -> buildKey(group, key))
          .collect(Collectors.toList()));
      return null;
    });
  }

  @Override
  public void deleteGroup(String group) {
    deleteByPattern(group, "*");
  }

  @Override
  public void deleteByPattern(String group, String pattern) {
    metricsRecorder.record("deleteByPattern", group, () -> {
      if (isBlank(pattern) || (isBlank(group) && "*".equals(pattern.trim()))) {
        return null;
      }
      deleteByScan(buildKey(group, pattern));
      return null;
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String group, String key) {
    return metricsRecorder.record("get", group,
        () -> (T) redisTemplate.opsForValue().get(buildKey(group, key)));
  }

  @Override
  public <T> T set(String group, String key, T value) {
    return metricsRecorder.record("set", group, () -> {
      redisTemplate.opsForValue().set(buildKey(group, key), value);
      return value;
    });
  }

  @Override
  public <T> T set(String group, String key, T value, long seconds) {
    return metricsRecorder.record("setWithTtl", group, () -> {
      redisTemplate.opsForValue().set(buildKey(group, key), value, seconds, TimeUnit.SECONDS);
      return value;
    });
  }

  @Override
  public Boolean setIfAbsent(String group, String key, Object value, long seconds) {
    return metricsRecorder.record("setIfAbsent", group, () -> redisTemplate.opsForValue()
        .setIfAbsent(buildKey(group, key), value, seconds, TimeUnit.SECONDS));
  }

  private String buildKey(String group, String key) {
    return keyBuilder.build(group, key);
  }

  private void deleteByScan(String pattern) {
    List<String> batch = new ArrayList<>(DELETE_BATCH_SIZE);
    ScanOptions options = ScanOptions.scanOptions().match(pattern).count(SCAN_COUNT).build();
    try (Cursor<String> cursor = redisTemplate.scan(options)) {
      while (cursor.hasNext()) {
        batch.add(cursor.next());
        if (batch.size() >= DELETE_BATCH_SIZE) {
          deleteBatch(batch);
        }
      }
    }
    deleteBatch(batch);
  }

  private void deleteBatch(List<String> keys) {
    if (keys.isEmpty()) {
      return;
    }
    redisTemplate.unlink(keys);
    keys.clear();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
