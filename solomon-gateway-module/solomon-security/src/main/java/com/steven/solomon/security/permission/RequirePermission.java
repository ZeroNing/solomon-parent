package com.steven.solomon.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限声明注解。
 *
 * <p>标记在 Controller 方法上，启动时由 {@link PermissionScanner} 扫描，
 * 自动生成权限编码并同步到 {@link PermissionStore}。</p>
 *
 * <p>权限编码根据接口路径自动生成（如 {@code /api/core/orders/{id}} → {@code CORE:ORDERS:ID}），
 * 也可通过 {@link #value()} 显式指定。权限名称取 Swagger {@code @Operation.summary}。</p>
 *
 * @author steven
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /** 权限编码，未指定时根据接口路径自动生成。 */
    String value() default "";

    /** 权限名称（展示用），未指定时取 Swagger summary 或方法名。 */
    String name() default "";

    /** 是否匿名接口（无需 Token 即可访问）。 */
    boolean anonymous() default false;
}
