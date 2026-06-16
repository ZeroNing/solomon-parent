package com.steven.solomon.context;

import cn.hutool.core.util.StrUtil;

/** 统一解析单租户和多租户模式下的有效租户编码。 */
public class TenantModeResolver {

  private final TenantModeProperties properties;

  public TenantModeResolver(TenantModeProperties properties) {
    this.properties = properties;
    validate();
  }

  /**
   * 解析租户编码。
   *
   * @return 有效租户编码；允许缺失时返回 null
   */
  public String resolve(String tenantCode) {
    String normalized = StrUtil.trim(tenantCode);
    if (StrUtil.isNotBlank(normalized)) {
      return normalized;
    }
    if (TenantMode.SINGLE.equals(properties.getMode())) {
      return properties.getDefaultCode();
    }
    if (properties.isRequireCodeInMultiMode()) {
      throw new IllegalArgumentException("多租户模式必须提供租户编码");
    }
    return null;
  }

  public boolean isSingleTenant() {
    return TenantMode.SINGLE.equals(properties.getMode());
  }

  public TenantModeProperties getProperties() {
    return properties;
  }

  private void validate() {
    if (properties == null || properties.getMode() == null) {
      throw new IllegalArgumentException("tenant.mode 不能为空");
    }
    if (TenantMode.SINGLE.equals(properties.getMode())
        && StrUtil.isBlank(properties.getDefaultCode())) {
      throw new IllegalArgumentException("单租户模式 tenant.default-code 不能为空");
    }
  }
}
