package com.steven.solomon.service;

import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;

public abstract class AbsICacheService implements  ICacheService {

  protected final Logger logger = LoggerUtils.logger(AbsICacheService.class);

  @Resource
  private CacheProfile properties;

  public String assembleKey(String group, String key) {
    StringBuilder sb = new StringBuilder();
    if(ValidateUtils.isNotEmpty(properties) && ValidateUtils.equalsIgnoreCase(SwitchModeEnum.TENANT_PREFIX.toString(),properties.getMode().toString())){
      String tenantCode = RequestHeaderHolder.getTenantCode();
      if(ValidateUtils.isEmpty(tenantCode)){
        logger.error("当前模式是:{},但是缺乏租户信息,所以不拼接",SwitchModeEnum.TENANT_PREFIX.getDesc());
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
