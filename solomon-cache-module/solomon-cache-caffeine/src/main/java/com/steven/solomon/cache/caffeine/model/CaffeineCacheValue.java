package com.steven.solomon.cache.caffeine.model;

/**
 * Caffeine 缓存值包装，支持每个 key 独立过期时间。
 */
public class CaffeineCacheValue {

  private final Object value;

  private final long expireAtMillis;

  public CaffeineCacheValue(Object value, long expireAtMillis) {
    this.value = value;
    this.expireAtMillis = expireAtMillis;
  }

  public Object getValue() {
    return value;
  }

  public long getExpireAtMillis() {
    return expireAtMillis;
  }

  public boolean isExpired() {
    return expireAtMillis > 0 && System.currentTimeMillis() >= expireAtMillis;
  }
}
