package com.steven.solomon.cache.caffeine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.steven.solomon.cache.caffeine.model.CaffeineCacheValue;
import com.steven.solomon.cache.caffeine.properties.CaffeineCacheProperties;
import com.steven.solomon.cache.caffeine.service.CaffeineCacheService;
import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.service.CacheService;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Caffeine 本地缓存自动装配。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "cache.caffeine", name = "enabled", havingValue = "true",
    matchIfMissing = false)
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class CaffeineCacheAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public com.github.benmanes.caffeine.cache.Cache<String, CaffeineCacheValue> caffeineNativeCache(
      CaffeineCacheProperties properties) {
    return Caffeine.newBuilder()
        .maximumSize(properties.getMaximumSize())
        .expireAfterAccess(properties.getDefaultExpire().toSeconds(), TimeUnit.SECONDS)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public CacheKeyBuilder caffeineCacheKeyBuilder(CaffeineCacheProperties properties) {
    return new CacheKeyBuilder(properties.getKey());
  }

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
