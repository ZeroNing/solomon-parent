package com.steven.solomon.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法结果缓存注解。
 *
 * <p>标注在方法上，自动缓存方法返回值。支持 SpEL 动态 key、条件缓存、
 * 过期时间控制，以及同步加载防止缓存击穿。</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheResult {

  /**
   * 缓存分组名称，用于隔离不同业务模块的缓存。
   */
  String group() default "";

  /**
   * 缓存 key，支持 SpEL 表达式（例如：#id、#param.id）。
   */
  String key() default "";

  /**
   * 缓存过期时间，单位秒。小于等于 0 表示永不过期。
   */
  long expireSeconds() default 0;

  /**
   * 方法返回 null 时是否仍然缓存 null 值，避免缓存穿透。
   */
  boolean cacheNull() default false;

  /**
   * SpEL 条件表达式，满足条件时才从缓存读取并写入缓存。
   */
  String condition() default "";

  /**
   * SpEL 排除表达式，满足条件时不写入缓存（可使用 #result 引用方法返回值）。
   */
  String unless() default "";

  /**
   * 是否对同一个缓存 key 启用同步加载，防止高并发场景下的缓存击穿。
   */
  boolean sync() default false;
}
