package com.steven.solomon.gatewaysecurity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要进行 Token 权限校验的接口。
 *
 * <p>启动时会自动扫描此注解，并将接口信息交给权限仓储保存。</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiPermission {

  /** 权限编码，建议使用稳定且唯一的业务编码。 */
  String value();

  /** 权限名称，用于后台权限管理页面展示。 */
  String name() default "";

  /** 权限说明。 */
  String description() default "";
}
