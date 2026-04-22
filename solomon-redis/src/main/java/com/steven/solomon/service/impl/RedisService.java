package com.steven.solomon.service.impl;

import com.steven.solomon.service.AbsICacheService;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis工具类
 *
 * @author ZENG.XIAO.YAN
 * @date 2018年6月7日
 */
public class RedisService extends AbsICacheService {

  private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // =============================common============================

  @Override
  public void expire(String group, String key, int time) {
    try {
      if (time > 0) {
        redisTemplate.expire(assembleKey(group, key), time, TimeUnit.SECONDS);
      }
    } catch (Exception e) {
      logger.error("设置Redis过期时间失败: group={}, key={}, time={}, error={}", group, key, time, e.getMessage(), e);
    }
  }

  @Override
  public Long getExpire(String group, String key) {
    try {
      return redisTemplate.getExpire(assembleKey(group, key), TimeUnit.SECONDS);
    } catch (Exception e) {
      logger.error("获取Redis过期时间失败: group={}, key={}, error={}", group, key, e.getMessage(), e);
      return -1L;
    }
  }

  @Override
  public boolean hasKey(String group, String key) {
    try {
      Boolean result = redisTemplate.hasKey(assembleKey(group, key));
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      logger.error("检查Redis key是否存在失败: group={}, key={}, error={}", group, key, e.getMessage(), e);
      return false;
    }
  }

  @Override
  public void del(String group, String... keys) {
    if (ValidateUtils.isEmpty(keys) || keys.length == 0) {
      return;
    }
    try {
      if (keys.length == 1) {
        redisTemplate.delete(assembleKey(group, keys[0]));
      } else {
        List<String> assembledKeys = Arrays.stream(keys)
            .map(key -> assembleKey(group, key))
            .collect(Collectors.toList());
        redisTemplate.delete(assembledKeys);
      }
    } catch (Exception e) {
      logger.error("删除Redis key失败: group={}, keys={}, error={}", group, Arrays.toString(keys), e.getMessage(), e);
    }
  }

  @Override
  public <T> T get(String group, String key) {
    try {
      String k = assembleKey(group, key);
      if (ValidateUtils.isEmpty(k)) {
        return null;
      }
      Object result = redisTemplate.opsForValue().get(k);
      return (T) result;
    } catch (Exception e) {
      logger.error("从Redis获取值失败: group={}, key={}, error={}", group, key, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public <T> T set(String group, String key, T value) {
    return set(group, key, value, 0);
  }

  @Override
  public <T> T set(String group, String key, T value, int time) {
    try {
      redisTemplate.opsForValue().set(assembleKey(group, key), value);
      if (time > 0) {
        expire(group, key, time);
      }
      return value;
    } catch (Exception e) {
      logger.error("设置Redis值失败: group={}, key={}, error={}", group, key, e.getMessage(), e);
      return value;
    }
  }

  @Override
  public Long delete(String group, String key, Object... hashKeys) {
    try {
      return redisTemplate.opsForHash().delete(assembleKey(group, key), hashKeys);
    } catch (Exception e) {
      logger.error("删除Redis hash field失败: group={}, key={}, hashKeys={}, error={}", 
          group, key, Arrays.toString(hashKeys), e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public boolean lockSet(String group, String key, Object value, int time) {
    try {
      Boolean success = redisTemplate.opsForValue().setIfAbsent(assembleKey(group, key), value);
      if (Boolean.TRUE.equals(success) && time > 0) {
        expire(group, key, time);
      }
      return Boolean.TRUE.equals(success);
    } catch (Exception e) {
      logger.error("设置Redis分布式锁失败: group={}, key={}, error={}", group, key, e.getMessage(), e);
      return false;
    }
  }

  @Override
  public void deleteLock(String group, String key) {
    try {
      redisTemplate.delete(assembleKey(group, key));
    } catch (Exception e) {
      logger.error("删除Redis分布式锁失败: group={}, key={}, error={}", group, key, e.getMessage(), e);
    }
  }

}
