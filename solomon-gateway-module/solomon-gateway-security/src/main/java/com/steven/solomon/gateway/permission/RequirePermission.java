package com.steven.solomon.gateway.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限声明注解。
 *
 * <p>标记在 Controller 方法上，网关启动时由 {@link PermissionScanner} 扫描，
 * 自动生成权限编码并同步到 {@link PermissionStore}。</p>
 *
 * <p>权限编码根据接口路径自动生成（如 {@code /api/core/orders/{id}} → {@code CORE:ORDERS:ID}），
 * 也可通过 {@link #value()} 显式指定。权限名称取 Swagger {@code @Operation.summary}，
 * 未配置时取方法名。</p>
 *
 * <pre>{@code
 * @Operation(summary = "订单详情")
 * @RequirePermission
 * @GetMapping("/api/core/orders/{id}")
 * public OrderVO detail(@PathVariable String id) { ... }
 *
 * // 匿名接口（登录、健康检查等）
 * @RequirePermission(anonymous = true)
 * @PostMapping("/api/auth/login")
 * public TokenVO login(@RequestBody LoginDTO dto) { ... }
 * }</pre>
 *
 * @author steven
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 权限编码。
     *
     * <p>未指定时根据接口路径自动生成。</p>
     */
    String value() default "";

    /**
     * 权限名称（展示用）。
     *
     * <p>未指定时取 Swagger {@code @Operation.summary}，再退而取方法名。</p>
     */
    String name() default "";

    /**
     * 是否匿名接口（无需 Token 即可访问）。
     *
     * <p>登录、注册、健康检查等公开接口设为 true，网关跳过 Token 校验。</p>
     */
    boolean anonymous() default false;
}
