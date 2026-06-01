package com.steven.solomon.gateway.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关 Swagger 聚合配置。
 */
@ConfigurationProperties("gateway.swagger")
public class GatewaySwaggerProperties {

  /**
   * 是否启用网关 Swagger 资源聚合。
   */
  private boolean enabled = true;

  /**
   * 下游服务 OpenAPI 文档路径。
   */
  private String apiDocsPath = "/v3/api-docs";

  /**
   * 不参与文档聚合的路由 ID。
   */
  private List<String> ignoredRouteIds = new ArrayList<>();

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

  public List<String> getIgnoredRouteIds() {
    return ignoredRouteIds;
  }

  public void setIgnoredRouteIds(List<String> ignoredRouteIds) {
    this.ignoredRouteIds = ignoredRouteIds;
  }
}
