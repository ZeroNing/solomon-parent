package com.steven.solomon.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重复请求限制注解。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatRequestLimit {

  /**
   * 锁定秒数，时间内同一 URL + token 只允许请求一次。
   */
  long lockSeconds() default 3;

  /**
   * 缓存分组。
   */
  String group() default "repeat-request";
}
