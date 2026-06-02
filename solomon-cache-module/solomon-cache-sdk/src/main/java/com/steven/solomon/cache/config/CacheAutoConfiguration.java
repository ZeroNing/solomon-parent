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
 * 缓存通用自动装配配置。
 *
 * <p>所有缓存实现（Redis / Caffeine）共享此配置中的注解切面能力。
 * 自动装配 {@link CacheExpressionResolver}、{@link RequestFingerprintBuilder}、
 * {@link CacheAspect} 和 {@link RepeatRequestLimitAspect}，
 * 并确保在具体缓存实现之后注入。</p>
 */
@AutoConfiguration
@AutoConfigureAfter(name = {
    "com.steven.solomon.cache.redis.config.RedisCacheAutoConfiguration",
    "com.steven.solomon.cache.caffeine.config.CaffeineCacheAutoConfiguration"
})
public class CacheAutoConfiguration {

  /**
   * 创建缓存 SpEL 表达式解析器 Bean。
   */
  @Bean
  @ConditionalOnMissingBean
  public CacheExpressionResolver cacheExpressionResolver() {
    return new CacheExpressionResolver();
  }

  /**
   * 创建请求指纹生成器 Bean。
   */
  @Bean
  @ConditionalOnMissingBean
  public RequestFingerprintBuilder requestFingerprintBuilder() {
    return new RequestFingerprintBuilder();
  }

  /**
   * 创建缓存注解切面 Bean（依赖 CacheService 存在且配置未禁用时生效）。
   */
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

  /**
   * 创建重复请求限制切面 Bean（依赖 CacheService 存在且配置未禁用时生效）。
   */
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
