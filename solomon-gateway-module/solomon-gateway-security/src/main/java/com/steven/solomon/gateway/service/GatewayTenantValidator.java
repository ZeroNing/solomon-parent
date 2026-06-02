package com.steven.solomon.gateway.service;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 租户有效性校验扩展点。
 *
 * <p>适用于登录等公开路径，也适用于已登录请求。引入模块可校验租户是否存在、是否启用。</p>
 */
@FunctionalInterface
public interface GatewayTenantValidator {

  Mono<Boolean> validate(String tenantCode, ServerWebExchange exchange);
}
