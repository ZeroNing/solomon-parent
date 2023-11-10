package com.steven.solomon.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.hash.Hash;
import com.steven.solomon.service.AbsICacheService;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
  public <T extends Set> T setGet(String group, String key) {
    return (T) redisTemplate.opsForSet().members(assembleKey(group,key));
  }

  @Override
  public <T extends Set> T set(String group, String key, int time, T... value) {
    redisTemplate.opsForSet().add(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return (T) CollectionUtil.set(false,value);
  }

  @Override
  public <T extends Set> T set(String group, String key, T... value) {
    return set(group,key,0,value);
  }

  @Override
  public Long remove(String group, String key, Object... value) {
    return redisTemplate.opsForSet().remove(assembleKey(group,key),value);
  }

  @Override
  public <T> T ListGet(String group, String key,int start,int end) {
    return (T) redisTemplate.opsForList().range(assembleKey(group,key),start,end);
  }

  @Override
  public <T> T leftPush(String group, String key, T value, int time) {
    redisTemplate.opsForList().leftPush(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return value;
  }

  @Override
  public <T> T leftPush(String group, String key, T value) {
    return leftPush(group,key,value,0);
  }

  @Override
  public <T> T leftPushAll(String group, String key, int time, T... value) {
    redisTemplate.opsForList().leftPushAll(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return (T) value;
  }

  @Override
  public <T> T leftPushAll(String group, String key, int time, List<T> value) {
    redisTemplate.opsForList().leftPushAll(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return (T) value;
  }

  @Override
  public <T> T leftPushAll(String group, String key, T... value) {
    return leftPushAll(group,key,0,value);
  }

  @Override
  public <T> T leftPushAll(String group, String key, List<T> value) {
    return leftPushAll(group,key,0,value);
  }

  @Override
  public <T> T rightPush(String group, String key, T value, int time) {
    redisTemplate.opsForList().rightPush(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return value;
  }

  @Override
  public <T> T rightPushAll(String group, String key, int time, T... value) {
    redisTemplate.opsForList().rightPushAll(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return (T) value;
  }

  @Override
  public <T> T rightPushAll(String group, String key, int time, List<T> value) {
    redisTemplate.opsForList().rightPushAll(assembleKey(group,key),value);
    if(time > 0){
      expire(group,key,time);
    }
    return (T) value;
  }

  @Override
  public <T> T rightPushAll(String group, String key, T... value) {
    return rightPushAll(group,key,0,value);
  }

  @Override
  public <T> T rightPushAll(String group, String key, List<T> value) {
    return rightPushAll(group,key,0,value);
  }

  @Override
  public <T> T rightPush(String group, String key, T value) {
    return rightPush(group,key,value);
  }

  @Override
  public <T> T hashGet(String group, String key, String hashKey) {
    return (T) redisTemplate.opsForHash().get(assembleKey(group,key),hashKey);
  }

  @Override
  public <T> T put(String group, String key, String hashKey, T value) {
    return put(group,key,hashKey,value,0);
  }

  @Override
  public <T> T put(String group, String key, String hashKey, T value, int time) {
    redisTemplate.opsForHash().put(assembleKey(group,key),hashKey,value);
    if(time > 0){
      expire(group,key,time);
    }
    return value;
  }

  @Override
  public <T extends Map> T putAll(String group, String key, T value, int time) {
    redisTemplate.opsForHash().putAll(assembleKey(group,key), value);
    if(time > 0){
      expire(group,key,time);
    }
    return value;
  }

  @Override
  public <T extends Map> T putAll(String group, String key, T value) {
    return putAll(group,key,value,0);
  }

  @Override
  public Long delete(String group, String key, Object... hashKey) {
    return redisTemplate.opsForHash().delete(assembleKey(group,key),hashKey);
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
