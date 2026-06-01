package com.steven.solomon.gateway.service;

import org.springframework.web.server.ServerWebExchange;

/**
 * 网关租户访问校验扩展点。
 *
 * <p>业务方可以覆盖默认 Bean，接入租户停用、套餐过期或路由访问范围校验。</p>
 */
@FunctionalInterface
public interface GatewayTenantAccessValidator {

  /**
   * 判断当前租户是否允许访问。
   */
  boolean validate(String tenantCode, ServerWebExchange exchange);
}
