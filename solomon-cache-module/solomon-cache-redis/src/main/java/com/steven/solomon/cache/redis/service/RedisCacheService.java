package com.steven.solomon.cache.redis.service;

import com.steven.solomon.cache.key.CacheKeyBuilder;
import com.steven.solomon.cache.service.CacheService;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

/**
 * 基于 Redis 的缓存服务实现。
 *
 * <p>使用 {@link RedisTemplate} 操作 Redis，通过 #{@link CacheKeyBuilder}
 * 自动生成完整的缓存 key。支持过期设置、查询、删除、按 pattern 删除（SCAN 命令）、
 * 读写和原子写入（setIfAbsent）等操作。</p>
 */
public class RedisCacheService implements CacheService {

  /** Redis 操作模板。 */
  private final RedisTemplate<String, Object> redisTemplate;

  /** 缓存 key 构建器。 */
  private final CacheKeyBuilder keyBuilder;

  public RedisCacheService(RedisTemplate<String, Object> redisTemplate, CacheKeyBuilder keyBuilder) {
    this.redisTemplate = redisTemplate;
    this.keyBuilder = keyBuilder;
  }

  @Override
  public void expire(String group, String key, long seconds) {
    redisTemplate.expire(buildKey(group, key), seconds, TimeUnit.SECONDS);
  }

  @Override
  public Long getExpire(String group, String key) {
    return redisTemplate.getExpire(buildKey(group, key), TimeUnit.SECONDS);
  }

  @Override
  public Boolean hasKey(String group, String key) {
    return redisTemplate.hasKey(buildKey(group, key));
  }

  @Override
  public void delete(String group, String... keys) {
    if (keys == null || keys.length == 0) {
      return;
    }
    redisTemplate.delete(Arrays.stream(keys)
        .filter(Objects::nonNull)
        .map(key -> buildKey(group, key))
        .collect(Collectors.toList()));
  }

  @Override
  public void deleteGroup(String group) {
    deleteByPattern(group, "*");
  }

  @Override
  public void deleteByPattern(String group, String pattern) {
    if (isBlank(pattern)) {
      return;
    }
    Set<String> keys = scanKeys(buildKey(group, pattern));
    if (!keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String group, String key) {
    return (T) redisTemplate.opsForValue().get(buildKey(group, key));
  }

  @Override
  public <T> T set(String group, String key, T value) {
    redisTemplate.opsForValue().set(buildKey(group, key), value);
    return value;
  }

  @Override
  public <T> T set(String group, String key, T value, long seconds) {
    redisTemplate.opsForValue().set(buildKey(group, key), value, seconds, TimeUnit.SECONDS);
    return value;
  }

  @Override
  public Boolean setIfAbsent(String group, String key, Object value, long seconds) {
    return redisTemplate.opsForValue()
        .setIfAbsent(buildKey(group, key), value, seconds, TimeUnit.SECONDS);
  }

  /** 使用 keyBuilder 组装完整缓存 key。 */
  private String buildKey(String group, String key) {
    return keyBuilder.build(group, key);
  }

  /**
   * 使用 SCAN 命令查找匹配 pattern 的 key，避免 KEYS 命令的阻塞问题。
   * 每次扫描 1000 条，直到遍历完所有匹配 key。
   */
  private Set<String> scanKeys(String pattern) {
    Set<String> keys = new LinkedHashSet<>();
    ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
    try (Cursor<String> cursor = redisTemplate.scan(options)) {
      while (cursor.hasNext()) {
        keys.add(cursor.next());
      }
    }
    return keys;
  }

  /** 判断字符串是否为 null 或空白。 */
  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
