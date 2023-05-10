package com.steven.solomon.condition;

import com.steven.solomon.enums.SwitchModeEnum;
import com.steven.solomon.verification.ValidateUtils;
import java.util.Map;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MongoDbCondition implements Condition {

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

    Environment environment = conditionContext.getEnvironment();
    String type = environment.getProperty("spring.data.mongodb.mode");
    return !ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(),type);
  }
}
