package com.steven.solomon.cache.key;

/**
 * 缓存 key 配置。
 */
public class CacheKeyProperties {

  private CacheKeyMode mode = CacheKeyMode.NONE;

  private String globalPrefix;

  private String separator = ":";

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
