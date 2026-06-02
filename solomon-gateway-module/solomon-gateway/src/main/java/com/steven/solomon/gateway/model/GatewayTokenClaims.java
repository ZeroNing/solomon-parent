package com.steven.solomon.gateway.model;

/** Token 中仅保存用户标识和租户编码。 */
public record GatewayTokenClaims(String userId, String tenantCode) {
}
