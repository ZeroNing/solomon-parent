package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisTenantContext extends TenantContext<RedisConnectionFactory> {

  @Override
  public RedisConnectionFactory getFactory() {
    return THREAD_LOCAL.get();
  }

  @Override
  public void setFactory(String key) {
    THREAD_LOCAL.set(FACTORY_MAP.get(key));
  }

  @Override
  public void removeFactory() {
    THREAD_LOCAL.remove();
  }

  @Override
  public Map<String, RedisConnectionFactory> getFactoryMap() {
    return FACTORY_MAP;
  }

  @Override
  public void setFactory(Map<String, RedisConnectionFactory> factoryMap) {
    FACTORY_MAP.putAll(factoryMap);
  }

  @Override
  public void setFactory(String key, RedisConnectionFactory factory) {
    FACTORY_MAP.put(key,factory);
  }

}
