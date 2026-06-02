package com.steven.solomon.context;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 全局租户运行模式配置。 */
@ConfigurationProperties("tenant")
public class TenantModeProperties {

  /** 默认使用单租户模式，便于单机应用开箱即用。 */
  private TenantMode mode = TenantMode.SINGLE;

  /** 单租户模式下使用的默认租户编码。 */
  private String defaultCode = "default";

  /** 多租户模式下是否强制要求请求携带租户编码。 */
  private boolean requireCodeInMultiMode = true;

  public TenantMode getMode() {
    return mode;
  }

  public void setMode(TenantMode mode) {
    this.mode = mode;
  }

  public String getDefaultCode() {
    return defaultCode;
  }

  public void setDefaultCode(String defaultCode) {
    this.defaultCode = defaultCode;
  }

  public boolean isRequireCodeInMultiMode() {
    return requireCodeInMultiMode;
  }

  public void setRequireCodeInMultiMode(boolean requireCodeInMultiMode) {
    this.requireCodeInMultiMode = requireCodeInMultiMode;
  }
}
