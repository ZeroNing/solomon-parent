package com.steven.solomon.gateway.permission;

/** 可保存到数据库或配置中心的接口权限元数据。 */
public record ApiPermissionMetadata(
    String value, String name, String path, String method, boolean anonymous) {
}
