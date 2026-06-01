package com.steven.solomon.gatewaysecurity.context;

import com.steven.solomon.gatewaysecurity.model.AccessTokenClaims;

/** 保存当前请求的多租户鉴权上下文。 */
public final class AuthContext {

  private static final ThreadLocal<AccessTokenClaims> CONTEXT = new ThreadLocal<>();

  private AuthContext() {
  }

  public static void set(AccessTokenClaims claims) {
    CONTEXT.set(claims);
  }

  public static AccessTokenClaims get() {
    return CONTEXT.get();
  }

  public static String getTenantCode() {
    AccessTokenClaims claims = get();
    return claims == null ? null : claims.tenantCode();
  }

  public static void clear() {
    CONTEXT.remove();
  }
}
