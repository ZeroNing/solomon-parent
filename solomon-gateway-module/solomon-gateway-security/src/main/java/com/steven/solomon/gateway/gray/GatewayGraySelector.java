package com.steven.solomon.gateway.gray;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gateway.filter.TenantGatewayFilter;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayGrayProperties;
import org.springframework.web.server.ServerWebExchange;

/** 根据可信租户、用户和请求路径稳定选择发布版本。 */
public class GatewayGraySelector {

  private final GatewayGrayProperties properties;

  public GatewayGraySelector(GatewayGrayProperties properties) {
    properties.validate();
    this.properties = properties;
  }

  public String select(ServerWebExchange exchange) {
    GatewayTokenClaims claims = exchange.getAttribute(TenantGatewayFilter.CLAIMS_ATTRIBUTE);
    String tenantCode = claims == null
        ? exchange.getAttribute(TenantGatewayFilter.TENANT_ATTRIBUTE) : claims.tenantCode();
    String userId = claims == null ? null : claims.userId();
    String key = StrUtil.join("|", StrUtil.nullToEmpty(tenantCode), StrUtil.nullToEmpty(userId),
        exchange.getRequest().getPath().value());
    return Math.floorMod(key.hashCode(), 100) < properties.getCandidateWeight()
        ? properties.getCandidateVersion() : properties.getStableVersion();
  }
}
