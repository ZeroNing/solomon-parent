package com.steven.solomon.gatewaysecurity.service;

import com.steven.solomon.gatewaysecurity.model.AccessTokenClaims;

/** 校验租户是否有效，可扩展租户禁用、套餐过期等业务判断。 */
@FunctionalInterface
public interface TenantVerifier {

  boolean isAvailable(AccessTokenClaims claims);
}
