package com.steven.solomon.security.properties;

import com.steven.solomon.security.constant.SecurityHeaders;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 多租户鉴权配置。
 */
@ConfigurationProperties("security.tenant")
public class SecurityTenantProperties {

  /**
   * 是否强制要求 Token 携带租户编码。
   */
  private boolean required = true;

  /**
   * 租户编码请求头。
   */
  private String headerName = SecurityHeaders.TENANT_CODE;

  /**
   * 是否校验请求头租户与 Token 租户一致。
   */
  private boolean validateHeader = true;

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getHeaderName() {
    return headerName;
  }

  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  public boolean isValidateHeader() {
    return validateHeader;
  }

  public void setValidateHeader(boolean validateHeader) {
    this.validateHeader = validateHeader;
  }
}
