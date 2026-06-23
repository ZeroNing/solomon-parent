package com.steven.solomon.cache.key;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 缓存 key 构建配置属性。
 *
 * <p>定义缓存 key 的组装模式、全局前缀、分隔符以及租户编码缺失时的行为。
 * 配合 {@link CacheKeyBuilder} 使用。</p>
 */
public class CacheKeyProperties {

  /** key 组装模式，默认 {@link CacheKeyMode#NONE}。 */
  @NotNull(message = "cache key mode must not be null")
  private CacheKeyMode mode = CacheKeyMode.NONE;

  /** 全局前缀，用于区分不同应用或环境。 */
  private String globalPrefix;

  /** key 各部分之间的分隔符，默认为 ":"。 */
  @NotBlank(message = "cache key separator must not be blank")
  @Size(max = 8, message = "cache key separator must be no longer than 8 characters")
  private String separator = ":";

  /**
   * 租户编码缺失时是否抛出异常。仅在 TENANT_PREFIX 模式下生效。
   */
  private boolean failWhenTenantMissing = false;

  public CacheKeyMode getMode() {
    return mode;
  }

  public void setMode(CacheKeyMode mode) {
    this.mode = mode;
  }

  public String getGlobalPrefix() {
    return globalPrefix;
  }

  public void setGlobalPrefix(String globalPrefix) {
    this.globalPrefix = globalPrefix;
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public boolean isFailWhenTenantMissing() {
    return failWhenTenantMissing;
  }

  public void setFailWhenTenantMissing(boolean failWhenTenantMissing) {
    this.failWhenTenantMissing = failWhenTenantMissing;
  }
}
