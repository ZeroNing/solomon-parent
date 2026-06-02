package com.steven.solomon.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Swagger 路由聚合配置。 */
@ConfigurationProperties("gateway.swagger")
public class GatewaySwaggerProperties {

  /** 是否启用 Swagger 资源聚合。 */
  private boolean enabled = true;

  /** 下游 OpenAPI 文档路径。 */
  private String apiDocsPath = "/v3/api-docs";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getApiDocsPath() {
    return apiDocsPath;
  }

  public void setApiDocsPath(String apiDocsPath) {
    this.apiDocsPath = apiDocsPath;
  }
}
