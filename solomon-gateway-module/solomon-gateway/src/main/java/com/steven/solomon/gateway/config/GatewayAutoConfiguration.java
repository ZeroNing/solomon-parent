package com.steven.solomon.gateway.config;

import com.steven.solomon.gateway.filter.GatewayTenantFilter;
import com.steven.solomon.gateway.properties.GatewayI18nProperties;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import com.steven.solomon.gateway.properties.GatewaySwaggerProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.service.GatewayTenantAccessValidator;
import com.steven.solomon.gateway.swagger.GatewaySwaggerResourceProvider;
import com.steven.solomon.gateway.utils.GatewayLocaleUtils;
import com.steven.solomon.gateway.utils.GatewayTenantUtils;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;

/**
 * 网关 SDK 自动配置。
 *
 * <p>配置入口统一使用 {@code gateway.*}，避免业务项目被固定项目前缀绑定。</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "gateway.sdk", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties({
    GatewayJwtProperties.class,
    GatewayTenantProperties.class,
    GatewaySwaggerProperties.class,
    GatewayI18nProperties.class
})
public class GatewayAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public JwtTokenUtils jwtTokenUtils(GatewayJwtProperties properties) {
    return new JwtTokenUtils(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public GatewayTenantUtils gatewayTenantUtils(
      JwtTokenUtils jwtTokenUtils,
      GatewayTenantProperties properties,
      GatewayTenantAccessValidator tenantAccessValidator) {
    return new GatewayTenantUtils(jwtTokenUtils, properties, tenantAccessValidator);
  }

  @Bean
  @ConditionalOnMissingBean
  public GatewayTenantAccessValidator gatewayTenantAccessValidator() {
    return (tenantCode, exchange) -> true;
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "gateway.tenant", name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public GatewayTenantFilter gatewayTenantFilter(GatewayTenantUtils gatewayTenantUtils) {
    return new GatewayTenantFilter(gatewayTenantUtils);
  }

  @Bean
  @ConditionalOnMissingBean
  public GatewayLocaleUtils gatewayLocaleUtils(GatewayI18nProperties properties) {
    return new GatewayLocaleUtils(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(RouteLocator.class)
  @ConditionalOnProperty(prefix = "gateway.swagger", name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public GatewaySwaggerResourceProvider gatewaySwaggerResourceProvider(
      RouteLocator routeLocator,
      GatewaySwaggerProperties properties) {
    return new GatewaySwaggerResourceProvider(routeLocator, properties);
  }
}
