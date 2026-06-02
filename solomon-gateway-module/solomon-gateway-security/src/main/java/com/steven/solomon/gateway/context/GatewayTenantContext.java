package com.steven.solomon.gateway.context;

import com.steven.solomon.gateway.filter.TenantGatewayFilter;
import org.springframework.web.server.ServerWebExchange;

/** 从当前网关请求中读取已校验的租户编码。 */
public final class GatewayTenantContext {

  private GatewayTenantContext() {
  }

  public static String getTenantCode(ServerWebExchange exchange) {
    return exchange.getAttribute(TenantGatewayFilter.TENANT_ATTRIBUTE);
  }
}
