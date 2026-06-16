package com.steven.solomon.gateway.swagger;

/**
 * Swagger 文档资源信息。
 *
 * @param name 服务名称（展示用）
 * @param url  api-docs 完整 URL
 * @author steven
 */
public record SwaggerResourceInfo(String name, String url) {
}
