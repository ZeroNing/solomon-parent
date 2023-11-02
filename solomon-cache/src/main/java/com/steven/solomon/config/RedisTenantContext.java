package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.profile.TenantRedisProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
@Configuration
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

}
