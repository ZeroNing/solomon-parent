package com.steven.solomon.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存删除注解。
 *
 * <p>标注在方法上，在方法执行前后按指定条件删除缓存数据，
 * 支持按单个 key、key 列表、pattern 匹配或清空整个分组删除。</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheRemove {

  /**
   * 缓存分组名称，用于隔离不同业务模块的缓存。
   */
  String group() default "";

  /**
   * 需要删除的缓存 key 列表，支持 SpEL 表达式（例如：#id、#param.code）。
   */
  String[] keys() default {};

  /**
   * 是否删除整个缓存分组下的所有缓存。
   */
  boolean allEntries() default false;

  /**
   * 按 pattern 匹配删除缓存 key，支持 SpEL 表达式。
   */
  String pattern() default "";

  /**
   * 是否在方法执行前删除缓存，false 表示在方法执行后删除。
   */
  boolean beforeInvocation() default false;
}
