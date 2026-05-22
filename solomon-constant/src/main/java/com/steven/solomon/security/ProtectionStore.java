package com.steven.solomon.security;

/**
 * 接口安全防护存储抽象。
 *
 * <p>防重复提交、防重放 nonce、防刷限流都依赖这个抽象保存临时状态。
 * 默认实现可以是内存，也可以由业务方替换为 Redis、MongoDB、数据库或其他分布式存储。
 * 只要业务项目声明自己的 {@code ProtectionStore} Bean，就可以覆盖框架默认实现。</p>
 */
public interface ProtectionStore {

  /**
   * 写入一个带过期时间的 key，仅当 key 不存在时写入成功。
   *
   * <p>典型用途: 幂等 key、nonce 防重放。</p>
   *
   * @param key 存储 key，调用方需要包含租户维度
   * @param ttlMillis 过期时间，单位毫秒
   * @return true 表示写入成功；false 表示 key 已存在
   */
  boolean putIfAbsent(String key, long ttlMillis);

  /**
   * 判断当前 key 在固定窗口内是否还允许访问。
   *
   * <p>典型用途: 接口限流。实现方需要保证并发下计数安全。</p>
   *
   * @param key 限流 key，调用方需要包含租户维度
   * @param permits 窗口内允许的请求数
   * @param windowMillis 窗口大小，单位毫秒
   * @return true 表示允许访问；false 表示超过限流阈值
   */
  boolean allow(String key, int permits, long windowMillis);
}
