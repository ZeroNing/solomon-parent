package com.steven.solomon.cache.caffeine.properties;

import com.steven.solomon.cache.key.CacheKeyProperties;
import java.time.Duration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Caffeine 本地缓存配置属性。
 *
 * <p>定义 Caffeine 缓存的启用开关、最大容量、默认过期时间和 key 生成规则。
 * 默认不启用，需设置 {@code cache.caffeine.enabled=true}。</p>
 */
@ConfigurationProperties(prefix = "cache.caffeine")
@Validated
public class CaffeineCacheProperties {

  /**
   * 是否启用 Caffeine 本地缓存。
   */
  private boolean enabled = false;

  /**
   * 最大缓存条目数。
   */
  @Min(value = 1, message = "cache.caffeine.maximum-size must be at least 1")
  private long maximumSize = 10_000;

  /**
   * 默认过期时间，业务没有通过 {@link com.steven.solomon.cache.annotation.CacheResult#expireSeconds()} 指定时使用此值。
   */
  @NotNull(message = "cache.caffeine.default-expire must not be null")
  private Duration defaultExpire = Duration.ofMinutes(30);

  /**
   * 缓存 key 生成规则配置。
   */
  @Valid
  @NotNull(message = "cache.caffeine.key must not be null")
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
