package com.steven.solomon.gatewaysecurity.config;

import com.steven.solomon.gatewaysecurity.support.PermissionRegistry;
import com.steven.solomon.gatewaysecurity.swagger.PermissionOpenApiCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** 多租户接口鉴权 Swagger 增强配置。 */
@AutoConfiguration(after = AuthAutoConfiguration.class)
@ConditionalOnClass({OpenAPI.class, OpenApiCustomizer.class})
@ConditionalOnProperty(prefix = "gateway.security.swagger", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class AuthSwaggerAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "permissionOpenApiCustomizer")
  public OpenApiCustomizer permissionOpenApiCustomizer(PermissionRegistry permissionRegistry) {
    return new PermissionOpenApiCustomizer(permissionRegistry);
  }
}
