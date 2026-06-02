package com.steven.solomon.cache.key;

import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存 key 组装工具。
 *
 * <p>根据 {@link CacheKeyMode} 配置，按 {@link CacheKeyProperties}
 * 自动组合全局前缀、租户编码、缓存分组和 key，形成完整的缓存 key 字符串。
 * 支持链式分隔符清理。</p>
 */
public class CacheKeyBuilder {

  /** 缓存 key 构建配置。 */
  private final CacheKeyProperties properties;

  public CacheKeyBuilder(CacheKeyProperties properties) {
    this.properties = properties == null ? new CacheKeyProperties() : properties;
  }

  /**
   * 构建完整的缓存 key。
   *
   * @param group 缓存分组
   * @param key   业务 key
   * @return 缓存 key 字符串
   * @throws IllegalArgumentException 当 key 为空时抛出
   */
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

  /**
   * 判断当前模式是否需要拼接全局前缀。
   * 除 NONE 模式外，PREFIX、TENANT_PREFIX、TENANT_SWITCH 都需要。
   */
  private boolean shouldUseGlobalPrefix() {
    return properties.getMode() == CacheKeyMode.PREFIX
        || properties.getMode() == CacheKeyMode.TENANT_PREFIX
        || properties.getMode() == CacheKeyMode.TENANT_SWITCH;
  }

  /**
   * 从请求上下文中解析当前租户编码。
   * 当缺少租户编码且配置为 failWhenTenantMissing=true 时抛出异常。
   */
  private String resolveTenantCode() {
    String tenantCode = RequestHeaderHolder.getTenantCode();
    if (isBlank(tenantCode) && properties.isFailWhenTenantMissing()) {
      throw new IllegalStateException("当前缓存模式需要租户编码，但上下文中没有 tenantCode");
    }
    return tenantCode;
  }

  /** 非空时添加到 parts 列表中。 */
  private void addIfPresent(List<String> parts, String value) {
    if (!isBlank(value)) {
      parts.add(trimSeparator(value));
    }
  }

  /** 移除字符串首尾的分隔符。 */
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

  /** 返回分隔符，默认为 ":"。 */
  private String separator() {
    return isBlank(properties.getSeparator()) ? ":" : properties.getSeparator();
  }

  /** 判断字符串是否为 null 或空白。 */
  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
