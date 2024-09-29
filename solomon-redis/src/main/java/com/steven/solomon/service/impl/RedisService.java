package com.steven.solomon.service.impl;

import com.steven.solomon.service.AbsICacheService;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    } catch (Throwable e) {
      logger.error("操作Redis出现异常",e);
    }
  }

  @Override
  public Long getExpire(String group, String key) {
    return redisTemplate.getExpire(assembleKey(group, key), TimeUnit.SECONDS);
  }

  @Override
  public boolean hasKey(String group, String key) {
    try {
      return ValidateUtils.getOrDefault(redisTemplate.hasKey(assembleKey(group, key)),false);
    } catch (Throwable e) {
      logger.error("操作Redis出现异常",e);
      return false;
    }
  }

  @Override
  public void del(String group, String... key) {

    if (ValidateUtils.isNotEmpty(key)) {
      if (key.length == 1) {
        redisTemplate.delete(assembleKey(group, key[0]));
      } else {
        List<String> keys = new ArrayList<>();
        Arrays.stream(key).forEach(a -> keys.add(assembleKey(group, a)));
        redisTemplate.delete(keys);
      }
    }
  }

  @Override
  public <T> T get(String group, String key) {
    String k = assembleKey(group, key);
    if(ValidateUtils.isEmpty(k)){
      return null;
    }
    T o = (T) redisTemplate.opsForValue().get(k);
    return ValidateUtils.getOrDefault(o,null);
  }

  @Override
  public <T> T set(String group, String key, T value) {
    return set(group,key,value,0);
  }

  @Override
  public <T> T set(String group, String key, T value, int time) {
    redisTemplate.opsForValue().set(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return value;
  }

  @Override
  public Long delete(String group, String key, Object... hashKey) {
    return redisTemplate.opsForHash().delete(assembleKey(group,key),hashKey);
  }

  @Override
  public boolean lockSet(String group, String key, Object value, int time) {
    Boolean success = redisTemplate.opsForValue().setIfAbsent(assembleKey(group, key), value);
    expire(group,key,time);
    return !ValidateUtils.getOrDefault(success,false);
  }

  @Override
  public void deleteLock(String group, String key) {
    redisTemplate.opsForValue().getOperations().delete(assembleKey(group, key));
  }

}
