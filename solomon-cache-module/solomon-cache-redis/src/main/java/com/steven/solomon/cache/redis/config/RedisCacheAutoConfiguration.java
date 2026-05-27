package com.steven.solomon.cache.redis.config;

import com.steven.solomon.cache.context.CacheTenantSwitcher;
import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.redis.connection.TenantAwareRedisConnectionFactory;
import com.steven.solomon.cache.redis.context.RedisCacheTenantContext;
import com.steven.solomon.cache.redis.factory.RedisConnectionFactoryBuilder;
import com.steven.solomon.cache.redis.properties.RedisCacheProperties;
import com.steven.solomon.cache.redis.service.RedisCacheService;
import com.steven.solomon.cache.service.CacheService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 缓存自动装配。
 */
@AutoConfiguration(before = RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "cache.redis", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(RedisCacheProperties.class)
public class RedisCacheAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public RedisCacheTenantContext redisCacheTenantContext() {
    return new RedisCacheTenantContext();
  }

  @Bean
  @ConditionalOnMissingBean
  public CacheTenantSwitcher<RedisConnectionFactory> redisCacheTenantSwitcher(
      RedisCacheTenantContext tenantContext) {
    return new CacheTenantSwitcher<>(tenantContext);
  }

  @Bean
  @ConditionalOnMissingBean
  public CacheKeyBuilder cacheKeyBuilder(RedisCacheProperties properties) {
    return new CacheKeyBuilder(properties.getKey());
  }

  @Bean
  @ConditionalOnMissingBean
  public RedisConnectionFactoryBuilder redisConnectionFactoryBuilder() {
    return new RedisConnectionFactoryBuilder();
  }

  @Bean("redisConnectionFactory")
  @ConditionalOnMissingBean(RedisConnectionFactory.class)
  public RedisConnectionFactory redisConnectionFactory(
      RedisCacheProperties cacheProperties,
      RedisCacheTenantContext tenantContext,
      RedisConnectionFactoryBuilder factoryBuilder) {
    Map<String, RedisProperties> tenantProperties = tenantProperties(cacheProperties);
    RedisConnectionFactory defaultConnectionFactory = null;
    RedisConnectionFactory firstConnectionFactory = null;
    for (Map.Entry<String, RedisProperties> entry : tenantProperties.entrySet()) {
      RedisConnectionFactory connectionFactory = factoryBuilder.build(entry.getValue());
      tenantContext.register(entry.getKey(), connectionFactory);
      if (firstConnectionFactory == null) {
        firstConnectionFactory = connectionFactory;
      }
      if (entry.getKey().equals(cacheProperties.getDefaultTenant())) {
        defaultConnectionFactory = connectionFactory;
      }
    }
    if (defaultConnectionFactory == null) {
      defaultConnectionFactory = firstConnectionFactory;
    }
    return new TenantAwareRedisConnectionFactory(tenantContext, defaultConnectionFactory);
  }

  @Bean("redisTemplate")
  @ConditionalOnMissingBean(name = "redisTemplate")
  public RedisTemplate<String, Object> redisTemplate(
      @Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(stringSerializer);
    redisTemplate.setHashKeySerializer(stringSerializer);
    redisTemplate.setValueSerializer(jsonSerializer);
    redisTemplate.setHashValueSerializer(jsonSerializer);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  @Bean("stringRedisTemplate")
  @ConditionalOnMissingBean(StringRedisTemplate.class)
  public StringRedisTemplate stringRedisTemplate(
      @Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public CacheService cacheService(
      @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
      CacheKeyBuilder keyBuilder) {
    return new RedisCacheService(redisTemplate, keyBuilder);
  }

  private Map<String, RedisProperties> tenantProperties(
      RedisCacheProperties cacheProperties) {
    Map<String, RedisProperties> tenants = new LinkedHashMap<>();
    if (cacheProperties.getTenants() != null) {
      tenants.putAll(cacheProperties.getTenants());
    }
    if (tenants.isEmpty()) {
      tenants.put(cacheProperties.getDefaultTenant(), cacheProperties);
    }
    return tenants;
  }
}
