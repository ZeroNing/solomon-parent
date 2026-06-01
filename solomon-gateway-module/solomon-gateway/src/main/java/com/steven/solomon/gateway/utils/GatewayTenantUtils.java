package com.steven.solomon.gateway.utils;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.service.GatewayTenantAccessValidator;
import java.util.List;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

/**
 * 网关租户工具。
 *
 * <p>统一完成可信租户解析、冲突校验、业务校验和下游请求头净化。</p>
 */
public class GatewayTenantUtils {

  private final JwtTokenUtils jwtTokenUtils;
  private final GatewayTenantProperties properties;
  private final GatewayTenantAccessValidator tenantAccessValidator;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public GatewayTenantUtils(
      JwtTokenUtils jwtTokenUtils,
      GatewayTenantProperties properties,
      GatewayTenantAccessValidator tenantAccessValidator) {
    this.jwtTokenUtils = jwtTokenUtils;
    this.properties = properties;
    this.tenantAccessValidator = tenantAccessValidator;
  }

  /**
   * 获取并校验当前请求的可信租户编码。
   */
  public String resolveTenantCode(ServerWebExchange exchange) {
    String token = exchange.getRequest().getHeaders().getFirst(GatewayHeaders.AUTHORIZATION);
    String headerTenantCode =
        exchange.getRequest().getHeaders().getFirst(properties.getHeaderName());
    String tokenTenantCode = resolveTokenTenantCode(token);
    if (properties.isValidateHeader()
        && StrUtil.isAllNotBlank(tokenTenantCode, headerTenantCode)
        && !StrUtil.equals(tokenTenantCode, headerTenantCode)) {
      throw new IllegalArgumentException("请求租户与 Token 租户不一致");
    }
    String tenantCode = StrUtil.isNotBlank(tokenTenantCode)
        ? tokenTenantCode
        : resolveHeaderTenantCode(headerTenantCode);
    if (properties.isRequired() && StrUtil.isBlank(tenantCode)) {
      throw new IllegalArgumentException("请求缺少租户编码");
    }
    if (StrUtil.isNotBlank(tenantCode) && !tenantAccessValidator.validate(tenantCode, exchange)) {
      throw new IllegalArgumentException("当前租户无权访问网关");
    }
    return tenantCode;
  }

  /**
   * 将可信租户编码写入下游请求头。
   *
   * <p>始终先删除外部传入值，避免未校验的租户头继续向下游传播。</p>
   */
  public ServerWebExchange writeTenantCode(ServerWebExchange exchange, String tenantCode) {
    return exchange.mutate()
        .request(builder -> builder.headers(headers -> {
          headers.remove(properties.getHeaderName());
          if (StrUtil.isNotBlank(tenantCode)) {
            headers.set(properties.getHeaderName(), tenantCode);
          }
        }))
        .build();
  }

  /**
   * 判断当前接口是否跳过租户校验。
   */
  public boolean isIgnoredPath(ServerWebExchange exchange) {
    String path = exchange.getRequest().getPath().value();
    List<String> ignoredPaths = properties.getIgnoredPaths();
    return ignoredPaths != null && ignoredPaths.stream()
        .filter(StrUtil::isNotBlank)
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  private String resolveTokenTenantCode(String token) {
    if (StrUtil.isBlank(token)) {
      return StrUtil.EMPTY;
    }
    if (!jwtTokenUtils.validateToken(token)) {
      if (properties.isRejectInvalidToken()) {
        throw new IllegalArgumentException("Token 无效或已过期");
      }
      return StrUtil.EMPTY;
    }
    return jwtTokenUtils.getTenantCode(token);
  }

  private String resolveHeaderTenantCode(String headerTenantCode) {
    return properties.isHeaderFallbackEnabled()
        ? StrUtil.emptyToDefault(headerTenantCode, StrUtil.EMPTY)
        : StrUtil.EMPTY;
  }
}
