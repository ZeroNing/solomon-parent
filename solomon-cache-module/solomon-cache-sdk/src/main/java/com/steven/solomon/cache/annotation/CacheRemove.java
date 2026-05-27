package com.steven.solomon.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 删除缓存注解。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheRemove {

  /**
   * 缓存分组，用于隔离不同业务。
   */
  String group() default "";

  /**
   * 需要删除的缓存 key，支持 SpEL。
   */
  String[] keys() default {};

  /**
   * 删除整个缓存分组。
   */
  boolean allEntries() default false;

  /**
   * 按 pattern 删除缓存，支持 SpEL。
   */
  String pattern() default "";

  /**
   * 是否在方法执行前删除缓存。
   */
  boolean beforeInvocation() default false;
}
