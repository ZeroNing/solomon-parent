package com.steven.solomon.config;

import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.service.ICacheService;
import com.steven.solomon.service.impl.DefaultService;
import com.steven.solomon.service.impl.RedisService;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value={CacheProperties.class})
public class CacheConfig {

  @Bean
  public ICacheService cacheService(CacheProperties cacheProperties){
    switch (cacheProperties.getType()) {
      case REDIS:
        return new RedisService();
      default:
        return new DefaultService();
    }
  }
}
