package com.steven.solomon.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.gateway.code.GatewayErrorCode;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.handler.GatewayErrorWriter;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.properties.GatewaySecurityProperties;
import com.steven.solomon.gateway.service.GatewayAccessValidator;
import com.steven.solomon.gateway.service.GatewayAnonymousPathProvider;
import com.steven.solomon.gateway.service.GatewayTenantValidator;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import java.util.List;
import java.util.Collection;
import org.springframework.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 解析 Token 并向下游透传可信租户信息。 */
public class TenantGatewayFilter implements GlobalFilter, Ordered {

  public static final String TENANT_ATTRIBUTE = TenantGatewayFilter.class.getName() + ".tenantCode";
  public static final String CLAIMS_ATTRIBUTE = TenantGatewayFilter.class.getName() + ".claims";
  private static final List<String> UNTRUSTED_HEADERS = List.of(
      GatewayHeaders.TENANT_CODE, GatewayHeaders.TENANT_ID, GatewayHeaders.TENANT_NAME,
      GatewayHeaders.USER_ID, "X-User-Roles", "X-User-Permissions");

  private final JwtTokenUtils tokenUtils;
  private final GatewayTenantProperties properties;
  private final GatewayAccessValidator accessValidator;
  private final GatewayTenantValidator tenantValidator;
  private final GatewayErrorWriter errorWriter;
  private final TenantModeResolver tenantModeResolver;
  private final GatewaySecurityProperties securityProperties;
  private final GatewayAnonymousPathProvider anonymousPathProvider;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public TenantGatewayFilter(JwtTokenUtils tokenUtils, GatewayTenantProperties properties,
      GatewayTenantValidator tenantValidator, GatewayAccessValidator accessValidator,
      GatewayErrorWriter errorWriter) {
    this(tokenUtils, properties, tenantValidator, accessValidator, errorWriter,
        new TenantModeResolver(new TenantModeProperties()), new GatewaySecurityProperties(),
        List::of);
  }

  public TenantGatewayFilter(JwtTokenUtils tokenUtils, GatewayTenantProperties properties,
      GatewayTenantValidator tenantValidator, GatewayAccessValidator accessValidator,
      GatewayErrorWriter errorWriter, TenantModeResolver tenantModeResolver,
      GatewaySecurityProperties securityProperties,
      GatewayAnonymousPathProvider anonymousPathProvider) {
    this.tokenUtils = tokenUtils;
    this.properties = properties;
    this.properties.validate();
    this.tenantValidator = tenantValidator;
    this.accessValidator = accessValidator;
    this.errorWriter = errorWriter;
    this.tenantModeResolver = tenantModeResolver;
    this.securityProperties = securityProperties;
    this.anonymousPathProvider = anonymousPathProvider;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (isIgnored(path)) {
      return filterIgnoredPath(exchange, chain, path);
    }
    String token = tokenUtils.resolveBearerToken(
        exchange.getRequest().getHeaders().getFirst(GatewayHeaders.AUTHORIZATION));
    if (StrUtil.isBlank(token)) {
      return errorWriter.write(exchange, GatewayErrorCode.TOKEN_REQUIRED);
    }
    GatewayTokenClaims claims = exchange.getAttribute(CLAIMS_ATTRIBUTE);
    if (claims == null) {
      claims = tokenUtils.parseToken(token);
    }
    if (claims == null) {
      return errorWriter.write(exchange, GatewayErrorCode.TOKEN_INVALID);
    }
    GatewayTokenClaims trustedClaims = claims;
    if (StrUtil.isBlank(trustedClaims.tenantCode())) {
      return errorWriter.write(exchange, GatewayErrorCode.TENANT_REQUIRED);
    }
    return validateTenant(trustedClaims.tenantCode(), exchange)
        .flatMap(validTenant -> Boolean.TRUE.equals(validTenant)
            ? validateAccess(trustedClaims, exchange) : Mono.just(false))
        .flatMap(allowed -> Boolean.TRUE.equals(allowed)
            ? chain.filter(writeTrustedHeaders(exchange, trustedClaims))
            : errorWriter.write(exchange, GatewayErrorCode.ACCESS_DENIED));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 100;
  }

  private boolean isIgnored(String path) {
    return matchesAny(properties.getIgnoredPaths(), path)
        || matchesAny(anonymousPathProvider.paths(), path);
  }

  private boolean matchesAny(Collection<String> patterns, String path) {
    if (CollUtil.isEmpty(patterns)) {
      return false;
    }
    return patterns.stream()
        .filter(StrUtil::isNotBlank)
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  private Mono<Void> filterIgnoredPath(ServerWebExchange exchange, GatewayFilterChain chain,
      String path) {
    if (!matchesAny(properties.getPublicTenantPaths(), path)) {
      return chain.filter(writeTrustedHeaders(exchange, null));
    }
    String rawTenantCode =
        exchange.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE);
    String tenantCode;
    try {
      tenantCode = tenantModeResolver.resolve(rawTenantCode);
    } catch (IllegalArgumentException ex) {
      return errorWriter.write(exchange, GatewayErrorCode.TENANT_REQUIRED);
    }
    if (StrUtil.isBlank(tenantCode)) {
      return chain.filter(writeTrustedHeaders(exchange, null));
    }
    if (!tokenUtils.isSafeIdentity(tenantCode)) {
      return errorWriter.write(exchange, GatewayErrorCode.TENANT_INVALID);
    }
    return validateTenant(tenantCode, exchange)
        .flatMap(allowed -> Boolean.TRUE.equals(allowed)
            ? chain.filter(writeTrustedTenantHeader(exchange, tenantCode))
            : errorWriter.write(exchange, GatewayErrorCode.ACCESS_DENIED));
  }

  private Mono<Boolean> validateTenant(String tenantCode, ServerWebExchange exchange) {
    return Mono.defer(() -> tenantValidator.validate(tenantCode, exchange))
        .defaultIfEmpty(false)
        .onErrorReturn(false);
  }

  private Mono<Boolean> validateAccess(GatewayTokenClaims claims, ServerWebExchange exchange) {
    return Mono.defer(() -> accessValidator.validate(claims, exchange))
        .defaultIfEmpty(false)
        .onErrorReturn(false);
  }

  private ServerWebExchange writeTrustedTenantHeader(ServerWebExchange exchange, String tenantCode) {
    ServerWebExchange trustedExchange = exchange.mutate().request(request -> request.headers(headers -> {
      removeUntrustedHeaders(headers);
      if (securityProperties.shouldForwardTrustedHeaders()) {
        headers.set(GatewayHeaders.TENANT_CODE, tenantCode);
      }
    })).build();
    trustedExchange.getAttributes().put(TENANT_ATTRIBUTE, tenantCode);
    return trustedExchange;
  }

  private ServerWebExchange writeTrustedHeaders(ServerWebExchange exchange,
      GatewayTokenClaims claims) {
    ServerWebExchange.Builder exchangeBuilder = exchange.mutate();
    exchangeBuilder.request(request -> {
      request.headers(headers -> {
        removeUntrustedHeaders(headers);
      });
      if (claims != null) {
        if (securityProperties.shouldForwardTrustedHeaders()) {
          request.header(GatewayHeaders.TENANT_CODE, claims.tenantCode());
          request.header(GatewayHeaders.USER_ID, claims.userId());
        }
      }
    });
    ServerWebExchange trustedExchange = exchangeBuilder.build();
    if (claims != null) {
      trustedExchange.getAttributes().put(TENANT_ATTRIBUTE, claims.tenantCode());
    }
    return trustedExchange;
  }

  private void removeUntrustedHeaders(HttpHeaders headers) {
    UNTRUSTED_HEADERS.forEach(headers::remove);
  }
}
