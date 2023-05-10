package com.steven.solomon.profile;

import com.steven.solomon.enums.CacheTypeEnum;
import com.steven.solomon.enums.SwitchModeEnum;
import java.util.Map;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.redis")
@Component
public class TenantRedisProperties {

  private Map<String,RedisProperties> tenant;

  /**
   * redis缓存模式（默认单库）
   */
  private String mode = SwitchModeEnum.NORMAL.toString();

  /**
   * 缓存类型
   */
  private String type = CacheTypeEnum.REDIS.toString();

  public Map<String, RedisProperties> getTenant() {
    return tenant;
  }

  public void setTenant(
      Map<String, RedisProperties> tenant) {
    this.tenant = tenant;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
