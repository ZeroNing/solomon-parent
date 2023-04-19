package com.steven.solomon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rabbitMQ重试次数注解
 * @author
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RabbitMqRetry {

  /**
   * 重试次数
   */
  int retryNumber() default 2;

  /**
   * 重试间隔 默认20秒
   */
  long initialInterval() default 20000L;

  /**
   * 最大重试间隔为100秒
   */
  long maxInterval() default 100000L;

  /**
   * 重试间隔乘法策略
   */
  double multiplier() default 5;
}
