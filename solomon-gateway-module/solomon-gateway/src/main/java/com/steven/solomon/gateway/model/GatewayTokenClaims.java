package com.steven.solomon.gateway.model;

import java.util.Map;

/**
 * 网关 Token 载荷。
 *
 * @param subject 用户唯一标识
 * @param tenantCode 租户编码
 * @param claims 业务扩展字段
 */
public record GatewayTokenClaims(String subject, String tenantCode, Map<String, Object> claims) {

  public GatewayTokenClaims(String subject, String tenantCode) {
    this(subject, tenantCode, Map.of());
  }
}
