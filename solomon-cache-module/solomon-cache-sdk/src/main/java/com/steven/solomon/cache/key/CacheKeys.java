package com.steven.solomon.cache.key;

/**
 * 缓存 key 工具入口，封装不处理、前缀、租户前缀、租户切换四类常用构建方式。
 */
public final class CacheKeys {

  private CacheKeys() {
  }

  public static CacheKeyBuilder none() {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.NONE);
    return new CacheKeyBuilder(properties);
  }

  public static CacheKeyBuilder prefix(String globalPrefix) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.PREFIX);
    properties.setGlobalPrefix(globalPrefix);
    return new CacheKeyBuilder(properties);
  }

  public static CacheKeyBuilder tenantPrefix(String globalPrefix) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.TENANT_PREFIX);
    properties.setGlobalPrefix(globalPrefix);
    return new CacheKeyBuilder(properties);
  }

  public static CacheKeyBuilder tenantPrefix(String globalPrefix, boolean failWhenTenantMissing) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.TENANT_PREFIX);
    properties.setGlobalPrefix(globalPrefix);
    properties.setFailWhenTenantMissing(failWhenTenantMissing);
    return new CacheKeyBuilder(properties);
  }

  public static CacheKeyBuilder tenantSwitch(String globalPrefix) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.TENANT_SWITCH);
    properties.setGlobalPrefix(globalPrefix);
    return new CacheKeyBuilder(properties);
  }
}
