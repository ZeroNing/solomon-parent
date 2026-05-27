package com.steven.solomon.cache.config;

import com.steven.solomon.cache.aspect.CacheAspect;
import com.steven.solomon.cache.aspect.CacheExpressionResolver;
import com.steven.solomon.cache.aspect.RepeatRequestLimitAspect;
import com.steven.solomon.cache.aspect.RequestFingerprintBuilder;
import com.steven.solomon.cache.service.CacheService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 缓存通用自动装配，所有缓存实现共享缓存注解能力。
 */
@AutoConfiguration
@AutoConfigureAfter(name = {
    "com.steven.solomon.cache.redis.config.RedisCacheAutoConfiguration",
    "com.steven.solomon.cache.caffeine.config.CaffeineCacheAutoConfiguration"
})
public class CacheAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public CacheExpressionResolver cacheExpressionResolver() {
    return new CacheExpressionResolver();
  }

  @Bean
  @ConditionalOnMissingBean
  public RequestFingerprintBuilder requestFingerprintBuilder() {
    return new RequestFingerprintBuilder();
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(CacheService.class)
  @ConditionalOnProperty(prefix = "cache.aspect", name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public CacheAspect cacheAspect(
      CacheService cacheService,
      CacheExpressionResolver expressionResolver) {
    return new CacheAspect(cacheService, expressionResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(CacheService.class)
  @ConditionalOnProperty(prefix = "cache.repeat-request", name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public RepeatRequestLimitAspect repeatRequestLimitAspect(
      CacheService cacheService,
      RequestFingerprintBuilder fingerprintBuilder) {
    return new RepeatRequestLimitAspect(cacheService, fingerprintBuilder);
  }
}
