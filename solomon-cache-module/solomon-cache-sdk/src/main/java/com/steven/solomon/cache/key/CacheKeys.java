package com.steven.solomon.cache.key;

/**
 * 缓存 key 工具入口。
 *
 * <p>封装四种常用 {@link CacheKeyMode} 的 {@link CacheKeyBuilder} 工厂方法：
 * <ul>
 *   <li>{@link #none()} - 不加工 key</li>
 *   <li>{@link #prefix(String)} - 加全局前缀</li>
 *   <li>{@link #tenantPrefix(String)} - 加全局前缀 + 租户编码</li>
 *   <li>{@link #tenantSwitch(String)} - 租户切换模式</li>
 * </ul>
 * </p>
 */
public final class CacheKeys {

  private CacheKeys() {
  }

  /**
   * 不处理 key，直接使用业务传入的值。
   */
  public static CacheKeyBuilder none() {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.NONE);
    return new CacheKeyBuilder(properties);
  }

  /**
   * 只增加全局前缀。
   *
   * @param globalPrefix 全局前缀
   */
  public static CacheKeyBuilder prefix(String globalPrefix) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.PREFIX);
    properties.setGlobalPrefix(globalPrefix);
    return new CacheKeyBuilder(properties);
  }

  /**
   * 全局前缀 + 租户编码前缀。
   *
   * @param globalPrefix 全局前缀
   */
  public static CacheKeyBuilder tenantPrefix(String globalPrefix) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.TENANT_PREFIX);
    properties.setGlobalPrefix(globalPrefix);
    return new CacheKeyBuilder(properties);
  }

  /**
   * 全局前缀 + 租户编码前缀，可配置租户缺失时是否抛出异常。
   *
   * @param globalPrefix          全局前缀
   * @param failWhenTenantMissing 租户编码缺失时是否抛出异常
   */
  public static CacheKeyBuilder tenantPrefix(String globalPrefix, boolean failWhenTenantMissing) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.TENANT_PREFIX);
    properties.setGlobalPrefix(globalPrefix);
    properties.setFailWhenTenantMissing(failWhenTenantMissing);
    return new CacheKeyBuilder(properties);
  }

  /**
   * 租户切换模式，key 本身不拼租户编码，
   * 通过切换到不同租户的缓存连接实现隔离。
   *
   * @param globalPrefix 全局前缀
   */
  public static CacheKeyBuilder tenantSwitch(String globalPrefix) {
    CacheKeyProperties properties = new CacheKeyProperties();
    properties.setMode(CacheKeyMode.TENANT_SWITCH);
    properties.setGlobalPrefix(globalPrefix);
    return new CacheKeyBuilder(properties);
  }
}
