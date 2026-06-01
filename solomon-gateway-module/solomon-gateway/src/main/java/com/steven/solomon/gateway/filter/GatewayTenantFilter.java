package com.steven.solomon.gateway.filter;

import com.steven.solomon.gateway.handler.GatewayTenantErrorWriter;
import com.steven.solomon.gateway.utils.GatewayTenantUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 多租户请求透传过滤器。
 */
public class GatewayTenantFilter implements GlobalFilter, Ordered {

  private final GatewayTenantUtils tenantUtils;

  public GatewayTenantFilter(GatewayTenantUtils tenantUtils) {
    this.tenantUtils = tenantUtils;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (tenantUtils.isIgnoredPath(exchange)) {
      return chain.filter(tenantUtils.writeTenantCode(exchange, null));
    }
    try {
      String tenantCode = tenantUtils.resolveTenantCode(exchange);
      return chain.filter(tenantUtils.writeTenantCode(exchange, tenantCode));
    } catch (IllegalArgumentException exception) {
      return GatewayTenantErrorWriter.write(exchange, exception.getMessage());
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 100;
  }
}
