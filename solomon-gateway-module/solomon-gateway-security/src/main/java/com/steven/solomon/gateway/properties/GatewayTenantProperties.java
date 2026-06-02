package com.steven.solomon.gateway.properties;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 网关多租户配置。 */
@ConfigurationProperties("gateway.tenant")
public class GatewayTenantProperties {

  /** 是否启用多租户过滤。 */
  private boolean enabled = true;

  /** 无需 Token 的路径，适用于登录、健康检查和 Swagger。 */
  private List<String> ignoredPaths = new ArrayList<>(List.of(
      "/auth/**", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**"));

  /** 公开路径中允许透传租户编码的路径，适用于多租户登录。 */
  private List<String> publicTenantPaths = new ArrayList<>(List.of("/auth/**"));

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<String> getIgnoredPaths() {
    return ignoredPaths;
  }

  public void setIgnoredPaths(List<String> ignoredPaths) {
    this.ignoredPaths = ignoredPaths;
  }

  public List<String> getPublicTenantPaths() {
    return publicTenantPaths;
  }

  public void setPublicTenantPaths(List<String> publicTenantPaths) {
    this.publicTenantPaths = publicTenantPaths;
  }

  /** 校验登录前租户透传路径必须同时属于匿名白名单。 */
  public void validate() {
    List<String> ignored = CollUtil.emptyIfNull(ignoredPaths).stream()
        .filter(StrUtil::isNotBlank)
        .toList();
    for (String path : CollUtil.emptyIfNull(publicTenantPaths)) {
      if (StrUtil.isNotBlank(path) && !ignored.contains(path)) {
        throw new IllegalArgumentException(
            "gateway.tenant.public-tenant-paths 必须同时配置在 gateway.tenant.ignored-paths 中: "
                + path);
      }
    }
  }
}
