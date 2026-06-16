package com.steven.solomon.gateway.permission;

/**
 * 权限元数据，描述一个受保护接口的权限信息。
 *
 * <p>由 {@link PermissionScanner} 扫描 {@link RequirePermission} 注解生成，
 * 存储到 {@link PermissionStore} 供授权校验和权限管理使用。</p>
 *
 * @param code      权限编码，如 {@code CORE:ORDERS:ID}
 * @param name      权限名称（展示用），如「订单详情」
 * @param path      接口路径，如 {@code /api/core/orders/{id}}
 * @param method    HTTP 方法，如 {@code GET}
 * @param anonymous 是否匿名接口
 * @author steven
 */
public record PermissionInfo(String code, String name, String path, String method, boolean anonymous) {
}
