package com.steven.solomon.cache.key;

import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存 key 组装工具。
 */
public class CacheKeyBuilder {

  private final CacheKeyProperties properties;

  public CacheKeyBuilder(CacheKeyProperties properties) {
    this.properties = properties == null ? new CacheKeyProperties() : properties;
  }

  public String build(String group, String key) {
    if (isBlank(key)) {
      throw new IllegalArgumentException("cache key 不能为空");
    }
    if (properties.getMode() == CacheKeyMode.NONE) {
      return trimSeparator(key);
    }
    List<String> parts = new ArrayList<>();
    if (shouldUseGlobalPrefix()) {
      addIfPresent(parts, properties.getGlobalPrefix());
    }
    if (properties.getMode() == CacheKeyMode.TENANT_PREFIX) {
      addIfPresent(parts, resolveTenantCode());
    }
    addIfPresent(parts, group);
    parts.add(key);
    return String.join(separator(), parts);
  }

  private boolean shouldUseGlobalPrefix() {
    return properties.getMode() == CacheKeyMode.PREFIX
        || properties.getMode() == CacheKeyMode.TENANT_PREFIX
        || properties.getMode() == CacheKeyMode.TENANT_SWITCH;
  }

  private String resolveTenantCode() {
    String tenantCode = RequestHeaderHolder.getTenantCode();
    if (isBlank(tenantCode) && properties.isFailWhenTenantMissing()) {
      throw new IllegalStateException("当前缓存模式需要租户编码，但上下文中没有 tenantCode");
    }
    return tenantCode;
  }

  private void addIfPresent(List<String> parts, String value) {
    if (!isBlank(value)) {
      parts.add(trimSeparator(value));
    }
  }

  private String trimSeparator(String value) {
    String separator = separator();
    while (value.startsWith(separator)) {
      value = value.substring(separator.length());
    }
    while (value.endsWith(separator)) {
      value = value.substring(0, value.length() - separator.length());
    }
    return value;
  }

  private String separator() {
    return isBlank(properties.getSeparator()) ? ":" : properties.getSeparator();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
