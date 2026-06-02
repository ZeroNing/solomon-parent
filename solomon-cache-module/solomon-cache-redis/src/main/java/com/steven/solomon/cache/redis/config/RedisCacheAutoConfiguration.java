package com.steven.solomon.cache.redis.config;

import com.steven.solomon.cache.context.CacheTenantSwitcher;
import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.redis.connection.TenantAwareRedisConnectionFactory;
import com.steven.solomon.cache.redis.context.RedisCacheTenantContext;
import com.steven.solomon.cache.redis.factory.RedisConnectionFactoryBuilder;
import com.steven.solomon.cache.redis.properties.RedisCacheProperties;
import com.steven.solomon.cache.redis.service.RedisCacheService;
import com.steven.solomon.cache.service.CacheService;
import com.steven.solomon.context.TenantRequestBinder;
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
 * Redis 缓存自动装配配置。
 *
 * <p>注册 Redis 多租户连接上下文、租户切换器、连接工厂、
 * RedisTemplate 和缓存服务。支持通过 {@code cache.redis.enabled} 关闭。</p>
 */
@AutoConfiguration(before = RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "cache.redis", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(RedisCacheProperties.class)
public class RedisCacheAutoConfiguration {

  /**
   * 创建 Redis 多租户连接上下文。
   */
  @Bean
  @ConditionalOnMissingBean
  public RedisCacheTenantContext redisCacheTenantContext() {
    return new RedisCacheTenantContext();
  }

  /**
   * 创建 Redis 缓存租户切换器。
   */
  @Bean
  @ConditionalOnMissingBean
  public CacheTenantSwitcher<RedisConnectionFactory> redisCacheTenantSwitcher(
      RedisCacheTenantContext tenantContext) {
    return new CacheTenantSwitcher<>(tenantContext);
  }

  /**
   * 将网关透传的租户编码绑定到当前请求的 Redis 连接上下文。
   */
  @Bean
  public TenantRequestBinder redisCacheTenantRequestBinder(RedisCacheTenantContext context) {
    return new TenantRequestBinder() {
      @Override
      public void bind(String tenantCode) {
        context.setFactory(tenantCode);
      }

      @Override
      public void clear() {
        context.removeFactory();
      }
    };
  }

  /**
   * 创建缓存 key 构建器。
   */
  @Bean
  @ConditionalOnMissingBean
  public CacheKeyBuilder cacheKeyBuilder(RedisCacheProperties properties) {
    return new CacheKeyBuilder(properties.getKey());
  }

  /**
   * 创建 Redis 连接工厂构建器。
   */
  @Bean
  @ConditionalOnMissingBean
  public RedisConnectionFactoryBuilder redisConnectionFactoryBuilder() {
    return new RedisConnectionFactoryBuilder();
  }

  /**
   * 创建多租户 Redis 连接工厂。
   * 遍历所有租户配置建立独立连接，并包装为 {@link TenantAwareRedisConnectionFactory}。
   */
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
      tenantContext.registerFactory(entry.getKey(), connectionFactory);
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

  /**
   * 创建 RedisTemplate（key 使用 String 序列化，value 使用 JSON 序列化）。
   */
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

  /**
   * 创建 StringRedisTemplate。
   */
  @Bean("stringRedisTemplate")
  @ConditionalOnMissingBean(StringRedisTemplate.class)
  public StringRedisTemplate stringRedisTemplate(
      @Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  /**
   * 创建 Redis 缓存服务实现。
   */
  @Bean
  @ConditionalOnMissingBean
  public CacheService cacheService(
      @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
      CacheKeyBuilder keyBuilder) {
    return new RedisCacheService(redisTemplate, keyBuilder);
  }

  /**
   * 组装多租户 Redis 配置映射。
   * 如果未配置 tenants，则使用默认配置作为唯一租户。
   */
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
