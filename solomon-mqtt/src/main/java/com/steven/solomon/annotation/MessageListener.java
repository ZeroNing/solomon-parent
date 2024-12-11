package com.steven.solomon.annotation;

import com.steven.solomon.config.MqttCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mqtt注解
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
@Conditional(MqttCondition.class)
public @interface MessageListener {

  /**
   * 主题
   */
  String[] topics();

  /**
   * 消息质量
   */
  int qos() default 0;

  /**
   * 允许订阅的租户范围
   */
  String[] tenantRange() default "";
}
