package com.steven.solomon.gatewaysecurity.service;

import com.steven.solomon.gatewaysecurity.model.AccessTokenClaims;
import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;

/** 校验当前 Token 是否拥有接口权限。 */
@FunctionalInterface
public interface PermissionVerifier {

  boolean hasPermission(AccessTokenClaims claims, PermissionDefinition permission);
}
