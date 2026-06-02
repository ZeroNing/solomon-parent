package com.steven.solomon.cache.service;

/**
 * 缓存服务统一接口。
 *
 * <p>定义缓存操作的核心方法，包括过期设置、查询、删除、
 * 按分组清空/匹配删除、读写和原子写入等。
 * 每种缓存实现（Redis / Caffeine）需要实现此接口。</p>
 */
public interface CacheService {

  /**
   * 设置 key 的过期时间。
   *
   * @param group   缓存分组
   * @param key     缓存 key
   * @param seconds 过期秒数
   */
  void expire(String group, String key, long seconds);

  /**
   * 获取 key 的剩余过期时间。
   *
   * @param group 缓存分组
   * @param key   缓存 key
   * @return 剩余秒数，-1 表示永不过期，-2 表示 key 不存在
   */
  Long getExpire(String group, String key);

  /**
   * 判断 key 是否存在。
   */
  Boolean hasKey(String group, String key);

  /**
   * 删除一个或多个缓存 key。
   *
   * @param group 缓存分组
   * @param keys  要删除的 key（可变参数）
   */
  void delete(String group, String... keys);

  /**
   * 删除整个缓存分组下的所有缓存。
   *
   * @param group 缓存分组
   */
  void deleteGroup(String group);

  /**
   * 按 pattern 匹配删除缓存。
   *
   * <p>Redis 实现使用 SCAN + DEL 命令，支持 * 通配符；
   * Caffeine 实现使用 Cache.asMap().keySet() 过滤。</p>
   *
   * @param group   缓存分组
   * @param pattern 匹配模式（支持 * 匹配任意字符）
   */
  void deleteByPattern(String group, String pattern);

  /**
   * 获取缓存值。
   *
   * @param group 缓存分组
   * @param key   缓存 key
   * @param <T>   返回值类型
   * @return 缓存的值，未命中返回 null
   */
  <T> T get(String group, String key);

  /**
   * 设置缓存（无过期时间）。
   */
  <T> T set(String group, String key, T value);

  /**
   * 设置缓存（指定过期秒数）。
   */
  <T> T set(String group, String key, T value, long seconds);

  /**
   * 原子写入：key 不存在时设置成功并返回 true，存在时返回 false。
   *
   * @param group   缓存分组
   * @param key     缓存 key
   * @param value   缓存值
   * @param seconds 过期秒数
   * @return 是否设置成功（true 表示之前不存在，已成功设置）
   */
  Boolean setIfAbsent(String group, String key, Object value, long seconds);
}
