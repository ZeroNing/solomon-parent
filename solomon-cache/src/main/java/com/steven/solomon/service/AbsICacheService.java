package com.steven.solomon.service;

import com.steven.solomon.enums.CacheModeEnum;
import com.steven.solomon.holder.HeardHolder;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.verification.ValidateUtils;

import javax.annotation.Resource;
import org.slf4j.Logger;

public abstract class AbsICacheService implements  ICacheService {

  private Logger logger = LoggerUtils.logger(AbsICacheService.class);

  @Resource
  private TenantRedisProperties properties;

  public String assembleKey(String group, String key) {
    StringBuilder sb = new StringBuilder();
    if(ValidateUtils.isNotEmpty(properties) && CacheModeEnum.TENANT_PREFIX.toString().equals(properties.getMode())){
      String tenantCode = HeardHolder.getTenantCode();
      if(ValidateUtils.isEmpty(tenantCode)){
        logger.error("当前模式是:{},但是缺乏租户信息,所以不拼接",CacheModeEnum.TENANT_PREFIX.getDesc());
      } else {
        sb.append(tenantCode).append(":");
      }
    }
    if (ValidateUtils.isNotEmpty(group)) {
      sb.append(group).append(":");
    }
    return sb.append(key).toString();
  }
}
