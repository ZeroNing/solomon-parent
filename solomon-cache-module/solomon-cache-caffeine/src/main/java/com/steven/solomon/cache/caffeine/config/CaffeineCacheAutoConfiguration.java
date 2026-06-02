package com.steven.solomon.cache.caffeine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.steven.solomon.cache.caffeine.model.CaffeineCacheValue;
import com.steven.solomon.cache.caffeine.properties.CaffeineCacheProperties;
import com.steven.solomon.cache.caffeine.service.CaffeineCacheService;
import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.key.CacheKeyMode;
import com.steven.solomon.cache.service.CacheService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Caffeine 本地缓存自动装配。
 *
 * <p>根据配置属性注册 Caffeine Cache 实例、缓存键生成器和缓存服务。
 * 默认不启用，需设置 {@code cache.caffeine.enabled=true}。</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "cache.caffeine", name = "enabled", havingValue = "true",
    matchIfMissing = false)
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class CaffeineCacheAutoConfiguration {

  /**
   * Caffeine 是本地缓存，不支持按租户切换连接资源。
   */
  @Bean
  public InitializingBean caffeineCacheModeValidator(CaffeineCacheProperties properties) {
    return () -> {
      if (properties.getKey().getMode() == CacheKeyMode.TENANT_SWITCH) {
        throw new IllegalStateException(
            "Caffeine 不支持 TENANT_SWITCH，请使用 TENANT_PREFIX 或切换到 Redis 缓存");
      }
    };
  }

  /**
   * 注册 Caffeine 原生缓存实例。
   *
   * @param properties Caffeine 缓存配置属性
   * @return Caffeine Cache 实例
   */
  @Bean
  @ConditionalOnMissingBean
  public com.github.benmanes.caffeine.cache.Cache<String, CaffeineCacheValue> caffeineNativeCache(
      CaffeineCacheProperties properties) {
    return Caffeine.newBuilder()
        .maximumSize(properties.getMaximumSize())
        .expireAfterAccess(properties.getDefaultExpire().toSeconds(), TimeUnit.SECONDS)
        .build();
  }

  /**
   * 注册 Caffeine 缓存键生成器。
   *
   * @param properties Caffeine 缓存配置属性
   * @return 缓存键生成器实例
   */
  @Bean
  @ConditionalOnMissingBean
  public CacheKeyBuilder caffeineCacheKeyBuilder(CaffeineCacheProperties properties) {
    return new CacheKeyBuilder(properties.getKey());
  }

  /**
   * 注册 Caffeine 缓存服务。
   *
   * @param caffeineNativeCache Caffeine 原生缓存实例
   * @param cacheKeyBuilder      缓存键生成器
   * @param properties           Caffeine 缓存配置属性
   * @return 缓存服务实例
   */
  @Bean
  @ConditionalOnMissingBean(CacheService.class)
  public CacheService caffeineCacheService(
      com.github.benmanes.caffeine.cache.Cache<String, CaffeineCacheValue> caffeineNativeCache,
      CacheKeyBuilder cacheKeyBuilder,
      CaffeineCacheProperties properties) {
    return new CaffeineCacheService(
        caffeineNativeCache,
        cacheKeyBuilder,
        properties.getDefaultExpire().toSeconds());
  }
}
