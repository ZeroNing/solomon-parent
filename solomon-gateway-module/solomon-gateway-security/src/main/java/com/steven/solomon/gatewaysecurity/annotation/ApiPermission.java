package com.steven.solomon.gatewaysecurity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要进行 Token 权限校验的接口。
 *
 * <p>启动时会自动扫描此注解，并将接口信息交给权限仓储保存。</p>
 * <p>value 和 name 留空时将自动从路径和 Swagger 注解中推导。</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiPermission {

  /**
   * 权限编码。
   * 留空时根据接口路径自动生成，例如 /api/core/orders/{id} → CORE:ORDERS:ID。
   */
  String value() default "";

  /**
   * 是否允许匿名访问。
   * 设为 true 时不强制校验 Token，但仍会注册该接口权限。
   */
  boolean anonymous() default false;

  /**
   * 权限名称，用于后台权限管理页面展示。
   * 留空时自动从 Swagger @Operation 注解的 summary 中提取。
   */
  String name() default "";

  /** 权限说明。 */
  String description() default "";
}
