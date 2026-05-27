package com.steven.solomon.cache.caffeine.properties;

import com.steven.solomon.cache.key.CacheKeyProperties;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Caffeine 本地缓存配置。
 */
@ConfigurationProperties(prefix = "cache.caffeine")
public class CaffeineCacheProperties {

  /**
   * 是否启用 Caffeine 本地缓存。
   */
  private boolean enabled = false;

  /**
   * 最大缓存条数。
   */
  private long maximumSize = 10_000;

  /**
   * 默认过期时间，业务没有指定过期秒数时使用。
   */
  private Duration defaultExpire = Duration.ofMinutes(30);

  /**
   * 缓存 key 生成规则。
   */
  private CacheKeyProperties key = new CacheKeyProperties();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public long getMaximumSize() {
    return maximumSize;
  }

  public void setMaximumSize(long maximumSize) {
    this.maximumSize = maximumSize;
  }

  public Duration getDefaultExpire() {
    return defaultExpire;
  }

  public void setDefaultExpire(Duration defaultExpire) {
    this.defaultExpire = defaultExpire;
  }

  public CacheKeyProperties getKey() {
    return key;
  }

  public void setKey(CacheKeyProperties key) {
    this.key = key;
  }
}
