package com.steven.solomon.security.model;

import java.util.Map;
import java.util.Set;

/**
 * Token 签发载荷。
 *
 * @param subject 用户唯一标识
 * @param tenantCode 租户编码
 * @param tenantId 租户 ID
 * @param tenantName 租户名称
 * @param authorities 权限集合
 * @param claims 业务扩展字段
 */
public record SecurityTokenClaims(
    String subject,
    String tenantCode,
    String tenantId,
    String tenantName,
    Set<String> authorities,
    Map<String, Object> claims) {

  public SecurityTokenClaims(String subject, String tenantCode, Set<String> authorities) {
    this(subject, tenantCode, null, null, authorities, Map.of());
  }
}
