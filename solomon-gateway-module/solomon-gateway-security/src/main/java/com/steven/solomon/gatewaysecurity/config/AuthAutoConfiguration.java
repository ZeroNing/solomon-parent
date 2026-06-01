package com.steven.solomon.gatewaysecurity.config;

import com.steven.solomon.gatewaysecurity.interceptor.PermissionInterceptor;
import com.steven.solomon.gatewaysecurity.properties.AuthProperties;
import com.steven.solomon.gatewaysecurity.scanner.PermissionEndpointScanner;
import com.steven.solomon.gatewaysecurity.service.PermissionRepository;
import com.steven.solomon.gatewaysecurity.service.PermissionVerifier;
import com.steven.solomon.gatewaysecurity.service.TenantVerifier;
import com.steven.solomon.gatewaysecurity.support.InMemoryPermissionRepository;
import com.steven.solomon.gatewaysecurity.support.PermissionRegistry;
import com.steven.solomon.gatewaysecurity.utils.TokenUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** 多租户接口鉴权自动配置。 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "gateway.security", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(AuthProperties.class)
public class AuthAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public TokenUtils tokenUtils(AuthProperties properties) {
    return new TokenUtils(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public PermissionRegistry permissionRegistry() {
    return new PermissionRegistry();
  }

  @Bean
  @ConditionalOnMissingBean(PermissionRepository.class)
  public InMemoryPermissionRepository permissionRepository() {
    return new InMemoryPermissionRepository();
  }

  @Bean
  @ConditionalOnMissingBean
  public PermissionVerifier permissionVerifier() {
    return (claims, permission) -> claims.permissions().contains(permission.code());
  }

  @Bean
  @ConditionalOnMissingBean
  public TenantVerifier tenantVerifier() {
    return claims -> true;
  }

  @Bean
  @ConditionalOnMissingBean
  public PermissionInterceptor permissionInterceptor(TokenUtils tokenUtils,
      TenantVerifier tenantVerifier, PermissionVerifier permissionVerifier) {
    return new PermissionInterceptor(tokenUtils, tenantVerifier, permissionVerifier);
  }

  @Bean
  @ConditionalOnMissingBean(name = "permissionWebMvcConfigurer")
  public WebMvcConfigurer permissionWebMvcConfigurer(PermissionInterceptor interceptor) {
    return new WebMvcConfigurer() {
      @Override
      public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
      }
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public PermissionEndpointScanner permissionEndpointScanner(
      ObjectProvider<RequestMappingHandlerMapping> handlerMappings,
      PermissionRepository permissionRepository, PermissionRegistry permissionRegistry) {
    return new PermissionEndpointScanner(handlerMappings, permissionRepository, permissionRegistry);
  }
}
