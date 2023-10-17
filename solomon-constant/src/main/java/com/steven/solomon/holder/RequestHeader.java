package com.steven.solomon.holder;

import java.io.Serializable;

public class RequestHeader implements Serializable {

  /**
   * 时区
   */
  private String timezone;

  /**
   * SAAS租户id
   */
  private String tenantId;
  /**
   * SAAS租户名称
   */
  private String tenantName;
  /**
   * SAAS租户编码
   */
  private String tenantCode;

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getTenantName() {
    return tenantName;
  }

  public void setTenantName(String tenantName) {
    this.tenantName = tenantName;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public void setTenantCode(String tenantCode) {
    this.tenantCode = tenantCode;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }
}
