package com.steven.solomon.gatewaysecurity.model;

/** 启动时扫描得到的接口权限定义。 */
public record PermissionDefinition(
    String code,
    String name,
    String description,
    String path,
    String httpMethod,
    boolean anonymous) {
}
