package com.steven.solomon.condition;

import com.steven.solomon.enums.CacheTypeEnum;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisCondition implements Condition {

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

    Environment environment = conditionContext.getEnvironment();
    String type = environment.getProperty("spring.redis.type");
    return ValidateUtils.equalsIgnoreCase(CacheTypeEnum.REDIS.toString(),type);
  }
}
