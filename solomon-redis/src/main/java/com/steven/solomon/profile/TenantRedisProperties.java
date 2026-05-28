package com.steven.solomon.profile;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Redis多租户配置属性。
 *
 * <p>通过 {@code spring.redis.tenant} 配置各租户独立的Redis连接参数，
 * key为租户编码，value为该租户的 {@link RedisProperties}。</p>
 */
@ConfigurationProperties(prefix = "spring.redis")
public class TenantRedisProperties {

  /** 租户编码与Redis配置的映射表 */
  private Map<String,RedisProperties> tenant;

  /**
   * 获取租户配置映射表。
   * @return 租户编码 -> Redis配置
   */
  public Map<String, RedisProperties> getTenant() {
    return tenant;
  }

  /**
   * 设置租户配置映射表。
   * @param tenant 租户编码 -> Redis配置
   */
  public void setTenant(
      Map<String, RedisProperties> tenant) {
    this.tenant = tenant;
  }
}
