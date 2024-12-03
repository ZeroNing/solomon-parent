package com.steven.solomon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.steven.solomon.config.MongoCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;
import org.springframework.stereotype.Component;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Component
@Conditional(MongoCondition.class)
public @interface MongoDBCapped {

  @AliasFor(annotation = Component.class)
  String value() default "";

  /**
   * 限制记录大小使用的是(字节)
   */
  long size() default 1024;

  /**
   * 限制记录行数
   */
  long maxDocuments() default 0;

}
