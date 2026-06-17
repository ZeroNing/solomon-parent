package com.steven.solomon.security.core.permission;

/**
 * 权限元数据，描述一个受保护接口的权限信息。
 *
 * @param code      权限编码，如 {@code CORE:ORDERS:ID}
 * @param name      权限名称（展示用），如「订单详情」
 * @param path      接口路径
 * @param method    HTTP 方法
 * @param anonymous 是否匿名接口
 * @author steven
 */
public record PermissionInfo(String code, String name, String path, String method, boolean anonymous) {
}
