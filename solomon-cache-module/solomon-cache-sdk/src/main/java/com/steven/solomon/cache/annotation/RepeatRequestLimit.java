package com.steven.solomon.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重复请求限制（防重复提交）注解。
 *
 * <p>标注在接口方法上，基于缓存对相同请求进行幂等控制。
 * 同一用户（token）+ 同一请求路径在锁定时间内只允许请求一次，
 * 超出重复请求直接返回已有结果或拒绝。</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatRequestLimit {

  /**
   * 锁定时间，单位秒。在此时间内同一请求被视为重复，不再执行方法体。
   */
  long lockSeconds() default 3;

  /**
   * 缓存分组名称，默认使用 "repeat-request"。
   */
  String group() default "repeat-request";
}
