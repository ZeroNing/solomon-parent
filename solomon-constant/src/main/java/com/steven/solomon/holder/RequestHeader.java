package com.steven.solomon.holder;

import java.io.Serializable;

/**
 * 请求头信息实体类，保存当前请求的租户与时区信息。
 *
 * <p>配合 {@link RequestHeaderHolder} 使用，通过 ThreadLocal 在整个调用链中传递请求上下文。</p>
 */
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
