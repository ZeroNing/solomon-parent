package com.steven.solomon.template;

import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class DynamicRedisTemplate<K,V> extends RedisTemplate<K,V> {

  public DynamicRedisTemplate(){
    super();
  }

  @Override
  public RedisConnectionFactory getConnectionFactory() {
    RedisConnectionFactory factory = RedisTenantContext.getFactory();
    return ValidateUtils.isEmpty(factory) ? super.getConnectionFactory() : factory;
  }
}
