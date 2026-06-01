package com.steven.solomon.security.constant;

/**
 * 安全模块统一请求头。
 */
public final class SecurityHeaders {

  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String TENANT_CODE = "X-Tenant-Code";

  private SecurityHeaders() {
  }
}
