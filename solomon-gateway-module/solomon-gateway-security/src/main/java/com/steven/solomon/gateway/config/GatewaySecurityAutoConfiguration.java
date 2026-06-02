package com.steven.solomon.gateway.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gateway.code.GatewayErrorCode;
import com.steven.solomon.gateway.handler.GatewayErrorWriter;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.service.GatewayAnonymousPathProvider;
import com.steven.solomon.gateway.security.GatewayBearerAuthenticationConverter;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

/** Spring Security 响应式鉴权配置。 */
@AutoConfiguration(after = GatewayAutoConfiguration.class)
@ConditionalOnClass({ServerHttpSecurity.class, SecurityWebFilterChain.class})
@ConditionalOnProperty(prefix = "gateway", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class GatewaySecurityAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(SecurityWebFilterChain.class)
  public SecurityWebFilterChain gatewaySecurityWebFilterChain(ServerHttpSecurity http,
      JwtTokenUtils tokenUtils, GatewayTenantProperties properties, GatewayErrorWriter errorWriter,
      GatewayAnonymousPathProvider anonymousPathProvider) {
    properties.validate();
    ReactiveAuthenticationManager authenticationManager =
        authentication -> Mono.just(authentication);
    AuthenticationWebFilter authenticationFilter =
        new AuthenticationWebFilter(authenticationManager);
    authenticationFilter.setSecurityContextRepository(
        NoOpServerSecurityContextRepository.getInstance());
    authenticationFilter.setServerAuthenticationConverter(
        new GatewayBearerAuthenticationConverter(tokenUtils));
    authenticationFilter.setAuthenticationFailureHandler(
        (exchange, exception) -> errorWriter.write(exchange.getExchange(),
            GatewayErrorCode.TOKEN_INVALID));

    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .logout(ServerHttpSecurity.LogoutSpec::disable)
        .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange(exchange -> exchange.anyExchange()
            .access((authentication, context) ->
                authorize(authentication, context, properties, anonymousPathProvider)))
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint((exchange, ex) ->
                errorWriter.write(exchange, GatewayErrorCode.TOKEN_REQUIRED))
            .accessDeniedHandler((exchange, ex) ->
                errorWriter.write(exchange, GatewayErrorCode.ACCESS_DENIED)))
        .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }

  private Mono<AuthorizationDecision> authorize(
      Mono<org.springframework.security.core.Authentication> authentication,
      AuthorizationContext context, GatewayTenantProperties properties,
      GatewayAnonymousPathProvider anonymousPathProvider) {
    String path = context.getExchange().getRequest().getPath().value();
    if (matchesAny(properties.getIgnoredPaths(), path)
        || matchesAny(anonymousPathProvider.paths(), path)) {
      return Mono.just(new AuthorizationDecision(true));
    }
    return authentication.map(value -> new AuthorizationDecision(value.isAuthenticated()))
        .defaultIfEmpty(new AuthorizationDecision(false));
  }

  private boolean matchesAny(java.util.Collection<String> patterns, String path) {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    if (CollUtil.isEmpty(patterns)) {
      return false;
    }
    return patterns.stream()
        .filter(StrUtil::isNotBlank)
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }
}
