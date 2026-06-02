package com.steven.solomon.context;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** 全局租户模式自动配置。 */
@AutoConfiguration
@EnableConfigurationProperties(TenantModeProperties.class)
public class TenantModeAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public TenantModeResolver tenantModeResolver(TenantModeProperties properties) {
    return new TenantModeResolver(properties);
  }
}
