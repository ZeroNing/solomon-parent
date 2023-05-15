package com.steven.solomon.template;

import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class DynamicRedisTemplate<K,V> extends RedisTemplate<K,V> {

  private final RedisTenantContext context;

  public DynamicRedisTemplate(RedisTenantContext context){
    super();
    this.context = context;
  }

  @Override
  public RedisConnectionFactory getConnectionFactory() {
    RedisConnectionFactory factory = null;
    if(ValidateUtils.isEmpty(SpringUtil.getApplicationContext())){
      factory  = context.getFactory();
    } else {
      factory = SpringUtil.getBean(RedisTenantContext.class).getFactory();
    }
    return ValidateUtils.isEmpty(factory) ? super.getConnectionFactory() : factory;
  }
}
