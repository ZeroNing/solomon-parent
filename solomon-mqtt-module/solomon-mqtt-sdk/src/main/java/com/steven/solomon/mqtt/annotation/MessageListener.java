package com.steven.solomon.mqtt.annotation;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.mqtt.condition.MqttEnabledCondition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * MQTT 消息监听器标记。
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Conditional(MqttEnabledCondition.class)
public @interface MessageListener {

  /**
   * 订阅主题。
   */
  String[] topics();

  /**
   * 消息质量等级。
   */
  int qos() default 0;

  /**
   * 允许订阅的租户范围。为空表示所有租户。
   */
  String[] tenantRange() default StrUtil.EMPTY;
}
