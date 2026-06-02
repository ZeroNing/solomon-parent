package com.steven.solomon.gateway.security;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gateway.code.GatewayErrorCode;
import com.steven.solomon.gateway.filter.TenantGatewayFilter;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import java.util.Collections;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 将已校验的 Bearer Token 转换为 Spring Security 身份。 */
public class GatewayBearerAuthenticationConverter implements ServerAuthenticationConverter {

  private final JwtTokenUtils tokenUtils;

  public GatewayBearerAuthenticationConverter(JwtTokenUtils tokenUtils) {
    this.tokenUtils = tokenUtils;
  }

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    String token = tokenUtils.resolveBearerToken(
        exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    if (StrUtil.isBlank(token)) {
      return Mono.empty();
    }
    GatewayTokenClaims claims = tokenUtils.parseToken(token);
    if (claims == null) {
      return Mono.error(new IllegalArgumentException(GatewayErrorCode.TOKEN_INVALID));
    }
    exchange.getAttributes().put(TenantGatewayFilter.CLAIMS_ATTRIBUTE, claims);
    return Mono.just(UsernamePasswordAuthenticationToken.authenticated(
        claims, null, Collections.emptyList()));
  }
}
