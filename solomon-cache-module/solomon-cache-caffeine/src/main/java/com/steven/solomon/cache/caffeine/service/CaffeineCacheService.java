package com.steven.solomon.cache.caffeine.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.steven.solomon.cache.caffeine.model.CaffeineCacheValue;
import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.service.CacheService;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 基于 Caffeine 的本地缓存实现。
 */
public class CaffeineCacheService implements CacheService {

  private final Cache<String, CaffeineCacheValue> cache;

  private final CacheKeyBuilder keyBuilder;

  private final long defaultExpireSeconds;

  public CaffeineCacheService(
      Cache<String, CaffeineCacheValue> cache,
      CacheKeyBuilder keyBuilder,
      long defaultExpireSeconds) {
    this.cache = cache;
    this.keyBuilder = keyBuilder;
    this.defaultExpireSeconds = defaultExpireSeconds;
  }

  @Override
  public void expire(String group, String key, long seconds) {
    String cacheKey = buildKey(group, key);
    CaffeineCacheValue value = cache.getIfPresent(cacheKey);
    if (value != null && !value.isExpired()) {
      cache.put(cacheKey, new CaffeineCacheValue(value.getValue(), expireAt(seconds)));
    }
  }

  @Override
  public Long getExpire(String group, String key) {
    CaffeineCacheValue value = cache.getIfPresent(buildKey(group, key));
    if (value == null || value.isExpired()) {
      return -2L;
    }
    if (value.getExpireAtMillis() <= 0) {
      return -1L;
    }
    long ttl = (value.getExpireAtMillis() - System.currentTimeMillis()) / 1000;
    return Math.max(ttl, 0);
  }

  @Override
  public Boolean hasKey(String group, String key) {
    return get(group, key) != null;
  }

  @Override
  public void delete(String group, String... keys) {
    if (keys == null || keys.length == 0) {
      return;
    }
    cache.invalidateAll(Arrays.stream(keys)
        .filter(Objects::nonNull)
        .map(key -> buildKey(group, key))
        .toList());
  }

  @Override
  public void deleteGroup(String group) {
    deleteByPattern(group, "*");
  }

  @Override
  public void deleteByPattern(String group, String pattern) {
    Pattern regex = Pattern.compile(toRegex(buildKey(group, pattern)));
    cache.invalidateAll(cache.asMap().keySet().stream()
        .filter(key -> regex.matcher(key).matches())
        .toList());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String group, String key) {
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

  @Override
  public <T> T set(String group, String key, T value) {
    cache.put(buildKey(group, key), new CaffeineCacheValue(value, expireAt(defaultExpireSeconds)));
    return value;
  }

  @Override
  public <T> T set(String group, String key, T value, long seconds) {
    cache.put(buildKey(group, key), new CaffeineCacheValue(value, expireAt(seconds)));
    return value;
  }

  @Override
  public Boolean setIfAbsent(String group, String key, Object value, long seconds) {
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
