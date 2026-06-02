package com.steven.solomon.gateway.config;

import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.gateway.filter.TenantGatewayFilter;
import com.steven.solomon.gateway.filter.GatewayGrayFilter;
import com.steven.solomon.gateway.gray.GatewayGraySelector;
import com.steven.solomon.gateway.permission.ApiPermissionCatalog;
import com.steven.solomon.gateway.permission.ApiPermissionStore;
import com.steven.solomon.gateway.permission.ApiPermissionScanner;
import com.steven.solomon.gateway.handler.GatewayErrorWriter;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import com.steven.solomon.gateway.properties.GatewayGrayProperties;
import com.steven.solomon.gateway.properties.GatewaySwaggerProperties;
import com.steven.solomon.gateway.properties.GatewaySecurityProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.service.GatewayAccessValidator;
import com.steven.solomon.gateway.service.GatewayAnonymousPathProvider;
import com.steven.solomon.gateway.service.GatewayTenantValidator;
import com.steven.solomon.gateway.swagger.SwaggerResourceProvider;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

/** Spring Cloud Gateway 多租户自动配置。 */
@AutoConfiguration
@ConditionalOnClass(GlobalFilter.class)
@ConditionalOnProperty(prefix = "gateway", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties({
    GatewayJwtProperties.class,
    GatewayGrayProperties.class,
    GatewaySecurityProperties.class,
    GatewayTenantProperties.class,
    GatewaySwaggerProperties.class
})
public class GatewayAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public JwtTokenUtils jwtTokenUtils(GatewayJwtProperties properties,
      TenantModeResolver tenantModeResolver) {
    return new JwtTokenUtils(properties, tenantModeResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  public GatewayErrorWriter gatewayErrorWriter() {
    return new GatewayErrorWriter();
  }

  @Bean
  @ConditionalOnMissingBean
  public ApiPermissionCatalog apiPermissionCatalog() {
    return new ApiPermissionCatalog();
  }

  @Bean
  @ConditionalOnMissingBean(ApiPermissionStore.class)
  public ApiPermissionStore apiPermissionStore(ApiPermissionCatalog catalog) {
    return catalog;
  }

  @Bean
  @ConditionalOnMissingBean(GatewayAnonymousPathProvider.class)
  public GatewayAnonymousPathProvider gatewayAnonymousPathProvider(ApiPermissionCatalog catalog) {
    return catalog;
  }

  @Bean
  @ConditionalOnMissingBean
  public ApiPermissionScanner apiPermissionScanner(ApplicationContext applicationContext,
      ApiPermissionStore permissionStore) {
    return new ApiPermissionScanner(applicationContext, permissionStore);
  }

  @Bean
  @ConditionalOnMissingBean
  public GatewayGraySelector gatewayGraySelector(GatewayGrayProperties properties) {
    return new GatewayGraySelector(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public GatewayGrayFilter gatewayGrayFilter(GatewayGrayProperties properties,
      GatewayGraySelector selector) {
    return new GatewayGrayFilter(properties, selector);
  }

  /**
   * 默认拒绝访问，业务系统必须覆盖此 Bean 实现租户、角色和权限校验。
   */
  @Bean
  @ConditionalOnMissingBean
  public GatewayAccessValidator gatewayAccessValidator() {
    return (claims, exchange) -> Mono.just(false);
  }

  /** 默认拒绝租户访问，业务系统必须覆盖并校验租户状态。 */
  @Bean
  @ConditionalOnMissingBean
  public GatewayTenantValidator gatewayTenantValidator() {
    return (tenantCode, exchange) -> Mono.just(false);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "gateway.tenant", name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public TenantGatewayFilter tenantGatewayFilter(JwtTokenUtils tokenUtils,
      GatewayTenantProperties properties, GatewayTenantValidator tenantValidator,
      GatewayAccessValidator accessValidator, GatewayErrorWriter errorWriter,
      TenantModeResolver tenantModeResolver, GatewaySecurityProperties securityProperties,
      GatewayAnonymousPathProvider anonymousPathProvider) {
    return new TenantGatewayFilter(tokenUtils, properties, tenantValidator, accessValidator,
        errorWriter, tenantModeResolver, securityProperties, anonymousPathProvider);
  }

  @Bean
  @ConditionalOnBean(RouteDefinitionLocator.class)
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "gateway.swagger", name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public SwaggerResourceProvider swaggerResourceProvider(RouteDefinitionLocator locator,
      GatewaySwaggerProperties properties) {
    return new SwaggerResourceProvider(locator, properties);
  }
}
