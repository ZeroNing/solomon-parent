package com.steven.solomon.base.profile;

import java.util.Map;

/**
 * 多租户配置基础模型。
 *
 * @param <T> 单租户 MQTT 配置类型
 */
public abstract class AbstractTenantProfile<T> {

  /**
   * 租户编码与 MQTT 配置映射。
   */
  private Map<String, T> tenant;

  /**
   * 是否启用 MQTT 模块。
   */
  private boolean enabled = true;

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, T> getTenant() {
    return tenant;
  }

  public void setTenant(Map<String, T> tenant) {
    this.tenant = tenant;
  }
}
