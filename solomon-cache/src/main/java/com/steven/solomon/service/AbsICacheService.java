package com.steven.solomon.service;

import com.steven.solomon.enums.CacheModeEnum;
import com.steven.solomon.holder.HeardHolder;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.verification.ValidateUtils;

import javax.annotation.Resource;

public abstract class AbsICacheService implements  ICacheService {

  @Resource
  private CacheProfile cacheProfile;

  public String assembleKey(String group, String key) {
    StringBuilder sb = new StringBuilder();
    if(ValidateUtils.isNotEmpty(cacheProfile) && CacheModeEnum.TENANT_PREFIX.toString().equals(cacheProfile.getMode())){
      String tenantId = HeardHolder.getTenantCode();
      if(ValidateUtils.isNotEmpty(tenantId)){
        sb.append(tenantId).append(":");
      }
    }
    if (ValidateUtils.isNotEmpty(group)) {
      sb.append(group).append(":");
    }
    return sb.append(key).toString();
  }
}
