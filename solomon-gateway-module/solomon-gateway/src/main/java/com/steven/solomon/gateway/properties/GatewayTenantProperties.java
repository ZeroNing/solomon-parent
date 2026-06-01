package com.steven.solomon.gateway.properties;

import com.steven.solomon.gateway.constant.GatewayHeaders;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关多租户配置。
 */
@ConfigurationProperties("gateway.tenant")
public class GatewayTenantProperties {

  /**
   * 是否启用租户校验和透传。
   */
  private boolean enabled = true;

  /**
   * 下游服务接收租户编码的请求头。
   */
  private String headerName = GatewayHeaders.TENANT_CODE;

  /**
   * Token 没有租户编码时，是否允许使用请求头租户编码。
   */
  private boolean headerFallbackEnabled = true;

  /**
   * 是否强制要求请求携带租户编码。
   */
  private boolean required = true;

  /**
   * 是否校验请求头租户与 Token 租户一致。
   */
  private boolean validateHeader = true;

  /**
   * 请求携带 Token 时，是否拒绝无效 Token。
   */
  private boolean rejectInvalidToken = true;

  /**
   * 无需校验租户的接口，例如登录和健康检查。
   */
  private List<String> ignoredPaths = new ArrayList<>(List.of(
      "/auth/**",
      "/actuator/health",
      "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html"));

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getHeaderName() {
    return headerName;
  }

  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  public boolean isHeaderFallbackEnabled() {
    return headerFallbackEnabled;
  }

  public void setHeaderFallbackEnabled(boolean headerFallbackEnabled) {
    this.headerFallbackEnabled = headerFallbackEnabled;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isValidateHeader() {
    return validateHeader;
  }

  public void setValidateHeader(boolean validateHeader) {
    this.validateHeader = validateHeader;
  }

  public boolean isRejectInvalidToken() {
    return rejectInvalidToken;
  }

  public void setRejectInvalidToken(boolean rejectInvalidToken) {
    this.rejectInvalidToken = rejectInvalidToken;
  }

  public List<String> getIgnoredPaths() {
    return ignoredPaths;
  }

  public void setIgnoredPaths(List<String> ignoredPaths) {
    this.ignoredPaths = ignoredPaths;
  }
}
