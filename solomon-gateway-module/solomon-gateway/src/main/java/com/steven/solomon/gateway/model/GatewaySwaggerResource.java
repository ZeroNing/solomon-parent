package com.steven.solomon.gateway.model;

/**
 * Swagger 聚合资源。
 *
 * @param name 路由名称
 * @param url 下游 OpenAPI 文档地址
 */
public record GatewaySwaggerResource(String name, String url) {
}
