package com.steven.solomon.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表注解。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

  /**
   * 表名。
   *
   * @return 数据库表名，优先级高于name
   */
  String value() default "";

  /**
   * 表名。
   *
   * @return 数据库表名；value为空时使用
   */
  String name() default "";
}
