package com.steven.solomon.mqtt.condition;

import cn.hutool.core.util.BooleanUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * MQTT 自动配置开关条件，统一读取 {@code mqtt.enabled} 配置。
 */
public abstract class AbstractMqttEnabledCondition implements Condition {

  private static final String MQTT_ENABLED_PROPERTY = "mqtt.enabled";

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String enabled = ValidateUtils.getOrDefault(context.getEnvironment().getProperty(MQTT_ENABLED_PROPERTY), "true");
    return BooleanUtil.toBoolean(enabled);
  }
}
