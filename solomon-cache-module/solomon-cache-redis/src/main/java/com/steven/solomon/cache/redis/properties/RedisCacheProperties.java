package com.steven.solomon.cache.redis.properties;

import com.steven.solomon.cache.key.CacheKeyProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 缓存模块配置属性。
 *
 * <p>继承 {@link RedisProperties}，扩展缓存启用开关、默认租户、
 * key 生成规则和多租户连接配置等。</p>
 */
@ConfigurationProperties(prefix = "cache.redis")
public class RedisCacheProperties extends RedisProperties {

  /**
   * 是否启用 Redis 缓存自动装配。
   */
  private boolean enabled = true;

  /**
   * 默认租户编码，未显式切换租户时使用。
   */
  private String defaultTenant = "default";

  /**
   * 缓存 key 生成规则配置。
   */
  private CacheKeyProperties key = new CacheKeyProperties();

  /**
   * 多租户 Redis 连接配置，key 为租户编码，value 为独立连接参数。
   */
  private Map<String, RedisProperties> tenants = new LinkedHashMap<>();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDefaultTenant() {
    return defaultTenant;
  }

  public void setDefaultTenant(String defaultTenant) {
    this.defaultTenant = defaultTenant;
  }

  public CacheKeyProperties getKey() {
    return key;
  }

  public void setKey(CacheKeyProperties key) {
    this.key = key;
  }

  public Map<String, RedisProperties> getTenants() {
    return tenants;
  }

  public void setTenants(Map<String, RedisProperties> tenants) {
    this.tenants = tenants;
  }
}
