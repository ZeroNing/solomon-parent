package com.steven.solomon.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.security.ProtectionStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口安全防护自动配置。
 *
 * <p>默认只注册内存存储。若配置 {@code solomon.security.protection.store-type=redis}，
 * 则由 solomon-redis 模块注册 Redis 存储。</p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SecurityProtectionProperties.class)
public class SecurityProtectionAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "solomon.security.protection", name = "store-type",
      havingValue = "memory", matchIfMissing = true)
  public ProtectionStore protectionStore() {
    return new InMemoryProtectionStore();
  }

  @Bean
  @ConditionalOnMissingBean
  public ApiSecurityProtectionFilter apiSecurityProtectionFilter(
      SecurityProtectionProperties properties,
      ProtectionStore protectionStore,
      ObjectMapper objectMapper) {
    return new ApiSecurityProtectionFilter(properties, protectionStore, objectMapper);
  }
}
