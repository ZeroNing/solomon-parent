package com.steven.solomon.condition;


import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Environment environment = context.getEnvironment();
    String      type      = environment.getProperty("spring.cache.type");
    return StrUtil.equalsIgnoreCase(CacheType.REDIS.name(),type);
  }
}
