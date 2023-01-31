package com.steven.solomon.condition;

import com.steven.solomon.enums.MqChoiceEnum;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class KafkaCondition implements Condition {

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    Environment environment = conditionContext.getEnvironment();
    String      choice         = environment.getProperty("mq.choice");
    return ValidateUtils.equalsIgnoreCase(choice, MqChoiceEnum.KAFKA.name());
  }
}
