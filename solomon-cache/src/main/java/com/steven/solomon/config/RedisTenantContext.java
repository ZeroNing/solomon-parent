package com.steven.solomon.config;

import com.steven.solomon.profile.TenantRedisProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public class RedisTenantContext {

  private static final ThreadLocal<RedisConnectionFactory> REDIS_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

  private static final Map<String, RedisConnectionFactory> REDIS_FACTORY_MAP = new ConcurrentHashMap<>();

  private static List<TenantRedisProperties> redisPropertiesList = new ArrayList<>();

  public static RedisConnectionFactory getFactory() {
    return RedisTenantContext.REDIS_FACTORY_THREAD_LOCAL.get();
  }

  public static void removeFactory() {
    RedisTenantContext.REDIS_FACTORY_THREAD_LOCAL.remove();
  }

  public static void setFactory(String name) {
    RedisTenantContext.REDIS_FACTORY_THREAD_LOCAL.set(REDIS_FACTORY_MAP.get(name));
  }

  public static void setProperties(TenantRedisProperties properties) {
    RedisTenantContext.redisPropertiesList.add(properties);
  }

  public static List<TenantRedisProperties> getProperties() {
    return RedisTenantContext.redisPropertiesList;
  }

  
  public static Map<String, RedisConnectionFactory> getFactoryMap() {
    return RedisTenantContext.REDIS_FACTORY_MAP;
  }

  
  public static void setFactoryMap(Map<String, RedisConnectionFactory> redisFactoryMap) {
    RedisTenantContext.REDIS_FACTORY_MAP.putAll(redisFactoryMap);
  }

  public static void setFactory(String tenantCode, RedisConnectionFactory factory) {
    RedisTenantContext.REDIS_FACTORY_MAP.put(tenantCode,factory);
  }
}
