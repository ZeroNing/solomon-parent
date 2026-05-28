package com.steven.solomon.cache.caffeine.model;

/**
 * Caffeine 缓存值包装，支持每个 key 独立过期时间。
 *
 * <p>Caffeine 本身不支持 per-key TTL，通过 {@link #expireAtMillis} 记录过期时间戳来模拟。</p>
 */
public class CaffeineCacheValue {

  /** 缓存值。 */
  private final Object value;

  /** 过期时间戳（毫秒），0 表示永不过期。 */
  private final long expireAtMillis;

  /**
   * 构造缓存值包装对象。
   *
   * @param value          缓存值
   * @param expireAtMillis 过期时间戳（毫秒）
   */
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
