package com.steven.solomon.gatewaysecurity.model;

import java.util.Collections;
import java.util.List;

/** Token 中保存的用户、租户和接口权限。 */
public record AccessTokenClaims(String userId, String tenantCode, List<String> permissions) {

  public AccessTokenClaims {
    permissions = permissions == null ? Collections.emptyList() : List.copyOf(permissions);
  }
}
