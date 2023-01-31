package com.steven.solomon.profile;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

public class TenantRedisProperties extends RedisProperties {

  private String tenantCode;

  public String getTenantCode() {
    return tenantCode;
  }

  public void setTenantCode(String tenantCode) {
    this.tenantCode = tenantCode;
  }

  public TenantRedisProperties() {
    super();
  }

  public TenantRedisProperties(RedisProperties properties){
    super();
    BeanUtils.copyProperties(properties, this);
    this.tenantCode = "default";
  }
}
