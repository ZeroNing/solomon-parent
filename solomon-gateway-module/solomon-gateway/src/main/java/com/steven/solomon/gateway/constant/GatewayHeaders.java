package com.steven.solomon.gateway.constant;

/**
 * 网关统一请求头。
 */
public final class GatewayHeaders {

  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String TENANT_CODE = "X-Tenant-Code";

  private GatewayHeaders() {
  }
}
