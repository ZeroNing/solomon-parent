package com.steven.solomon.cache.key;

/**
 * 缓存 key 组装模式。
 */
public enum CacheKeyMode {

  /**
   * 不处理 key，直接使用业务传入的 key。
   */
  NONE,

  /**
   * 只增加全局前缀。
   */
  PREFIX,

  /**
   * 增加租户前缀模式。
   */
  TENANT_PREFIX,

  /**
   * 多租户资源切换模式，key 本身不拼租户。
   */
  TENANT_SWITCH
}
