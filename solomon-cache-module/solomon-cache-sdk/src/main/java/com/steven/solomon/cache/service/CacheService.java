package com.steven.solomon.cache.service;

/**
 * 缓存服务统一接口。
 */
public interface CacheService {

  void expire(String group, String key, long seconds);

  Long getExpire(String group, String key);

  Boolean hasKey(String group, String key);

  void delete(String group, String... keys);

  void deleteGroup(String group);

  /**
   * 按缓存实现支持的 pattern 删除缓存，例如 Redis/Caffeine 使用 * 匹配任意字符。
   */
  void deleteByPattern(String group, String pattern);

  <T> T get(String group, String key);

  <T> T set(String group, String key, T value);

  <T> T set(String group, String key, T value, long seconds);

  Boolean setIfAbsent(String group, String key, Object value, long seconds);
}
