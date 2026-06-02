package com.steven.solomon.cache.key;

/**
 * 缓存 key 组装模式枚举。
 *
 * <p>定义缓存 key 的四种组装策略，控制是否拼接全局前缀和租户编码：
 * <ul>
 *   <li>{@link #NONE} - 不处理 key</li>
 *   <li>{@link #PREFIX} - 只加全局前缀</li>
 *   <li>{@link #TENANT_PREFIX} - 全局前缀 + 租户编码</li>
 *   <li>{@link #TENANT_SWITCH} - 全局前缀，通过数据源切换实现租户隔离</li>
 * </ul>
 * </p>
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
   * 增加全局前缀和租户编码前缀，实现 key 级别的租户隔离。
   */
  TENANT_PREFIX,

  /**
   * 多租户资源切换模式，key 本身不拼租户编码，
   * 通过切换到不同租户的缓存连接实现隔离。
   */
  TENANT_SWITCH
}
