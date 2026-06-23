package com.steven.solomon.cache.caffeine.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.steven.solomon.cache.caffeine.model.CaffeineCacheValue;
import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.metrics.CacheMetricsRecorder;
import com.steven.solomon.cache.service.CacheService;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CaffeineCacheService implements CacheService {

  private static final String METRIC_TOTAL = "solomon.cache.caffeine.operation.total";
  private static final String METRIC_DURATION = "solomon.cache.caffeine.operation.duration";
  private static final String METRIC_DURATION_DESCRIPTION = "Caffeine cache operation duration";

  private final Cache<String, CaffeineCacheValue> cache;
  private final CacheKeyBuilder keyBuilder;
  private final long defaultExpireSeconds;
  private final CacheMetricsRecorder metricsRecorder;

  public CaffeineCacheService(
      Cache<String, CaffeineCacheValue> cache,
      CacheKeyBuilder keyBuilder,
      long defaultExpireSeconds) {
    this(cache, keyBuilder, defaultExpireSeconds, null);
  }

  public CaffeineCacheService(
      Cache<String, CaffeineCacheValue> cache,
      CacheKeyBuilder keyBuilder,
      long defaultExpireSeconds,
      MeterRegistry meterRegistry) {
    this.cache = cache;
    this.keyBuilder = keyBuilder;
    this.defaultExpireSeconds = defaultExpireSeconds;
    this.metricsRecorder = new CacheMetricsRecorder(
        meterRegistry, METRIC_TOTAL, METRIC_DURATION, METRIC_DURATION_DESCRIPTION);
  }

  @Override
  public void expire(String group, String key, long seconds) {
    metricsRecorder.record("expire", group, () -> {
      String cacheKey = buildKey(group, key);
      CaffeineCacheValue value = cache.getIfPresent(cacheKey);
      if (value != null && !value.isExpired()) {
        cache.put(cacheKey, new CaffeineCacheValue(value.getValue(), expireAt(seconds)));
      }
      return null;
    });
  }

  @Override
  public Long getExpire(String group, String key) {
    return metricsRecorder.record("getExpire", group, () -> {
      CaffeineCacheValue value = cache.getIfPresent(buildKey(group, key));
      if (value == null || value.isExpired()) {
        return -2L;
      }
      if (value.getExpireAtMillis() <= 0) {
        return -1L;
      }
      long ttl = (value.getExpireAtMillis() - System.currentTimeMillis()) / 1000;
      return Math.max(ttl, 0);
    });
  }

  @Override
  public Boolean hasKey(String group, String key) {
    return metricsRecorder.record("hasKey", group, () -> getInternal(group, key) != null);
  }

  @Override
  public void delete(String group, String... keys) {
    metricsRecorder.record("delete", group, () -> {
      if (keys == null || keys.length == 0) {
        return null;
      }
      cache.invalidateAll(Arrays.stream(keys)
          .filter(Objects::nonNull)
          .map(key -> buildKey(group, key))
          .toList());
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
      Pattern regex = Pattern.compile(toRegex(buildKey(group, pattern)));
      cache.invalidateAll(cache.asMap().keySet().stream()
          .filter(key -> regex.matcher(key).matches())
          .toList());
      return null;
    });
  }

  @Override
  public <T> T get(String group, String key) {
    return metricsRecorder.record("get", group, () -> getInternal(group, key));
  }

  @Override
  public <T> T set(String group, String key, T value) {
    return setValue("set", group, key, value, defaultExpireSeconds);
  }

  @Override
  public <T> T set(String group, String key, T value, long seconds) {
    return setValue("setWithTtl", group, key, value, seconds);
  }

  @Override
  public Boolean setIfAbsent(String group, String key, Object value, long seconds) {
    return metricsRecorder.record("setIfAbsent", group, () -> {
      String cacheKey = buildKey(group, key);
      Map<String, CaffeineCacheValue> map = cache.asMap();
      CaffeineCacheValue current = map.get(cacheKey);
      if (current != null && !current.isExpired()) {
        return false;
      }
      if (current != null) {
        map.remove(cacheKey, current);
      }
      return map.putIfAbsent(cacheKey, new CaffeineCacheValue(value, expireAt(seconds))) == null;
    });
  }

  private <T> T setValue(String operation, String group, String key, T value, long seconds) {
    return metricsRecorder.record(operation, group, () -> {
      cache.put(buildKey(group, key), new CaffeineCacheValue(value, expireAt(seconds)));
      return value;
    });
  }

  @SuppressWarnings("unchecked")
  private <T> T getInternal(String group, String key) {
    String cacheKey = buildKey(group, key);
    CaffeineCacheValue value = cache.getIfPresent(cacheKey);
    if (value == null) {
      return null;
    }
    if (value.isExpired()) {
      cache.invalidate(cacheKey);
      return null;
    }
    return (T) value.getValue();
  }

  private String buildKey(String group, String key) {
    return keyBuilder.build(group, key);
  }

  private long expireAt(long seconds) {
    if (seconds <= 0) {
      return 0;
    }
    return System.currentTimeMillis() + seconds * 1000;
  }

  private String toRegex(String pattern) {
    return Pattern.quote(pattern).replace("*", "\\E.*\\Q");
  }
}
