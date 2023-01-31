package com.steven.solomon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.data.annotation.Persistent;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface MongoDBCapped {

  /**
   * 限制记录大小使用的是(字节)
   * @return
   */
  long size() default 1024;

  /**
   * 限制记录行数
   * @return
   */
  long maxDocuments() default 0;

}
