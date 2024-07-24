package com.steven.solomon.config;

import com.steven.solomon.service.ICacheService;
import com.steven.solomon.service.impl.DefaultService;
import com.steven.solomon.service.impl.RedisService;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value={CacheProperties.class})
public class CacheConfig {

  @Bean
  @ConditionalOnMissingBean(ICacheService.class)
  public ICacheService cacheService(){
    return new RedisService();
  }
}
