package com.steven.solomon.config;

import com.steven.solomon.condition.RedisCondition;
import com.steven.solomon.manager.DynamicDefaultRedisCacheWriter;
import com.steven.solomon.manager.SpringRedisAutoManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"springUtil"})
public class EhCacheConfig extends CachingConfigurerSupport {

  @Bean
  @Override
  @Conditional(value = RedisCondition.class)
  public CacheManager cacheManager(){
    RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().computePrefixWith((name -> name + ":"));
    SpringRedisAutoManager springRedisAutoManager = new SpringRedisAutoManager(DynamicDefaultRedisCacheWriter.nonLockingRedisCacheWriter(
        RedisTenantContext.getFactoryMap().values().iterator().next()), defaultCacheConfig);
    return springRedisAutoManager;
  }
}
