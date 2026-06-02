package com.steven.solomon.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库字段注解。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

  /**
   * 数据库字段名。
   *
   * <p>允许不填写。不填写时，ORM会把Java字段名自动转换为下划线字段名，
   * 例如 {@code userId -> user_id}、{@code createTime -> create_time}。</p>
   *
   * @return 数据库字段名；为空时使用Java字段名的下划线格式
   */
  String value() default "";

  /**
   * 插入时是否包含该字段。
   *
   * @return true表示insert时包含该字段
   */
  boolean insertable() default true;

  /**
   * 更新时是否包含该字段。
   *
   * @return true表示update时包含该字段
   */
  boolean updatable() default true;
}
