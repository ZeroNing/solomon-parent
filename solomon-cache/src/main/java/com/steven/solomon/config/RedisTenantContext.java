package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.profile.TenantRedisProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
@Configuration(proxyBeanMethods = false)
public class RedisTenantContext extends TenantContext<RedisConnectionFactory, TenantRedisProperties> {

  private ThreadLocal<RedisConnectionFactory> REDIS_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

  private Map<String, RedisConnectionFactory> REDIS_FACTORY_MAP = new ConcurrentHashMap<>();

  @Override
  public RedisConnectionFactory getFactory() {
    return REDIS_FACTORY_THREAD_LOCAL.get();
  }

  @Override
  public void setFactory(String key) {
    REDIS_FACTORY_THREAD_LOCAL.set(REDIS_FACTORY_MAP.get(key));
  }

  @Override
  public void removeFactory() {
    REDIS_FACTORY_THREAD_LOCAL.remove();
  }

  @Override
  public Map<String, RedisConnectionFactory> getFactoryMap() {
    return REDIS_FACTORY_MAP;
  }

  @Override
  public void setFactory(Map<String, RedisConnectionFactory> factoryMap) {
    REDIS_FACTORY_MAP.putAll(factoryMap);
  }

  @Override
  public void setFactory(String key, RedisConnectionFactory factory) {
    REDIS_FACTORY_MAP.put(key,factory);
  }

  @Override
  public Map<String, TenantRedisProperties> getPropertiesMap() {
    return null;
  }

  @Override
  public void setProperties(String key, TenantRedisProperties properties) {

  }

  @Override
  public void setProperties(Map<String, TenantRedisProperties> propertiesMap) {

  }

//  public static RedisConnectionFactory getFactory() {
//    return REDIS_FACTORY_THREAD_LOCAL.get();
//  }
//
//  public static void removeFactory() {
//    REDIS_FACTORY_THREAD_LOCAL.remove();
//  }
//
//  public static void setFactory(String name) {
//    REDIS_FACTORY_THREAD_LOCAL.set(REDIS_FACTORY_MAP.get(name));
//  }
//
//  public static void setProperties(TenantRedisProperties properties) {
//    redisPropertiesList.add(properties);
//  }
//
//  public static List<TenantRedisProperties> getProperties() {
//    return redisPropertiesList;
//  }
//
//
//  public static Map<String, RedisConnectionFactory> getFactoryMap() {
//    return REDIS_FACTORY_MAP;
//  }
//
//
//  public static void setFactoryMap(Map<String, RedisConnectionFactory> redisFactoryMap) {
//    REDIS_FACTORY_MAP.putAll(redisFactoryMap);
//  }
//
//  public static void setFactory(String tenantCode, RedisConnectionFactory factory) {
//    REDIS_FACTORY_MAP.put(tenantCode,factory);
//  }
}
