package com.steven.solomon.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法结果缓存注解。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheResult {

  /**
   * 缓存分组，用于隔离不同业务。
   */
  String group() default "";

  /**
   * 缓存 key，支持 SpEL，例如：#id、#param.id。
   */
  String key() default "";

  /**
   * 过期秒数，小于等于 0 时不过期。
   */
  long expireSeconds() default 0;

  /**
   * 是否缓存 null 结果。
   */
  boolean cacheNull() default false;

  /**
   * 满足条件才读取和写入缓存，支持 SpEL。
   */
  String condition() default "";

  /**
   * 满足条件时不写入缓存，支持 SpEL，可使用 #result。
   */
  String unless() default "";

  /**
   * 是否同步加载同一个缓存 key，避免热点 key 击穿。
   */
  boolean sync() default false;
}
