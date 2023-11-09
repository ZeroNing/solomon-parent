package com.steven.solomon.service.impl;

import com.steven.solomon.service.AbsICacheService;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis工具类
 *
 * @author ZENG.XIAO.YAN
 * @date 2018年6月7日
 */
public class RedisService extends AbsICacheService {

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  // =============================common============================

  @Override
  public boolean expire(String group, String key, int time) {
    try {
      if (time > 0) {
        redisTemplate.expire(assembleKey(group, key), time, TimeUnit.SECONDS);
      }
      return true;
    } catch (Throwable e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public long getExpire(String group, String key) {
    return redisTemplate.getExpire(assembleKey(group, key), TimeUnit.SECONDS);
  }

  @Override
  public boolean hasKey(String group, String key) {
    try {
      return redisTemplate.hasKey(assembleKey(group, key));
    } catch (Throwable e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void del(String group, String... key) {

    if (!ValidateUtils.isEmpty(key) && key.length > 0) {
      if (key.length == 1) {
        redisTemplate.delete(assembleKey(group, key[0]));
      } else {
        List<String> keys = new ArrayList<>();
        Arrays.asList(key).stream().forEach(a -> keys.add(assembleKey(group, a)));
        redisTemplate.delete(keys);
      }
    }
  }

  // ============================String=============================

  @Override
  public <T> T get(String group, String key) {
    String k = assembleKey(group, key);
    if(ValidateUtils.isEmpty(k)){
      return null;
    }
    return (T) redisTemplate.opsForValue().get(k);
  }

//  @Override
//  public <T> T getList(String group, String key) {
//    String k = assembleKey(group, key);
//    if(ValidateUtils.isEmpty(k)){
//      return null;
//    }
//    return (T)redisTemplate.opsForList().leftPop(k);
//  }
//
//  @Override
//  public <T> T getSet(String group, String key) {
//    String k = assembleKey(group, key);
//    if(ValidateUtils.isEmpty(k)){
//      return null;
//    }
//    return (T)redisTemplate.opsForSet().members(k);
//  }
//
//  @Override
//  public <T> T getMap(String group, String key) {
//    String k = assembleKey(group, key);
//    if(ValidateUtils.isEmpty(k)){
//      return null;
//    }
//    return (T)redisTemplate.opsForHash().entries(k);
//  }

  @Override
  public <T> T set(String group, String key, T value) {
    String assembleKey = assembleKey(group,key);
    redisTemplate.opsForValue().set(assembleKey,value);
    return value;
  }

  @Override
  public <T> T set(String group, String key, T value, int time) {
    set(group, key, value);
    if(time > 0){
      expire(group,key,time);
    }
    return value;
  }

  @Override
  public boolean lockSet(String group, String key, Object value, int time) {
    Boolean success = redisTemplate.opsForValue().setIfAbsent(assembleKey(group, key), value);
    expire(group,key,time);
    return !ValidateUtils.isEmpty(success) ? success : false;
  }

  @Override
  public void deleteLock(String group, String key) {
    redisTemplate.opsForValue().getOperations().delete(assembleKey(group, key));
  }

}
