package com.steven.solomon.condition;

import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MongoCondition implements Condition {

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

    Environment environment = conditionContext.getEnvironment();
    String uri = environment.getProperty("spring.data.mongodb.uri");
    String database = environment.getProperty("spring.data.mongodb.database");
    if(ValidateUtils.isEmpty(uri) || ValidateUtils.isEmpty(database)){
      return false;
    } else {
      return true;
    }
  }
}
