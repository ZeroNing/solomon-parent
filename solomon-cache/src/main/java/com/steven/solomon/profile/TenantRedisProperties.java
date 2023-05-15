package com.steven.solomon.profile;

import com.steven.solomon.enums.CacheTypeEnum;
import com.steven.solomon.enums.SwitchModeEnum;
import java.util.Map;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.redis")
public class TenantRedisProperties {

  private Map<String,RedisProperties> tenant;

  /**
   * redis缓存模式（默认单库）
   */
  private SwitchModeEnum mode = SwitchModeEnum.NORMAL;

  /**
   * 缓存类型
   */
  private CacheTypeEnum type = CacheTypeEnum.REDIS;

  public Map<String, RedisProperties> getTenant() {
    return tenant;
  }

  public void setTenant(
      Map<String, RedisProperties> tenant) {
    this.tenant = tenant;
  }

  public SwitchModeEnum getMode() {
    return mode;
  }

  public void setMode(SwitchModeEnum mode) {
    this.mode = mode;
  }

  public CacheTypeEnum getType() {
    return type;
  }

  public void setType(CacheTypeEnum type) {
    this.type = type;
  }
}
