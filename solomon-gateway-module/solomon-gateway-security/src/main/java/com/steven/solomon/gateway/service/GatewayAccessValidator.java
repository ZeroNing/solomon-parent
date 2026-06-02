package com.steven.solomon.gateway.service;

import com.steven.solomon.gateway.model.GatewayTokenClaims;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关访问校验扩展点。
 *
 * <p>引入模块可根据用户、租户和请求路径查询角色、权限，自定义路由访问规则。</p>
 */
@FunctionalInterface
public interface GatewayAccessValidator {

  Mono<Boolean> validate(GatewayTokenClaims claims, ServerWebExchange exchange);
}
