package com.steven.solomon.security.core;

/**
 * 令牌声明，承载从 JWT 解析出的用户标识与租户编码。
 *
 * <p>作为不可变记录，贯穿鉴权过滤器、授权校验器和灰度选择器，
 * 保证身份信息在处理链路中不被篡改。</p>
 *
 * @param userId     用户标识
 * @param tenantCode 租户编码
 * @author steven
 */
public record TokenClaims(String userId, String tenantCode) {
}
