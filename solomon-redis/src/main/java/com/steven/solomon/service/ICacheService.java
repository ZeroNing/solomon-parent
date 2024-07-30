package com.steven.solomon.service;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ICacheService {

  /**
   * 指定缓存失效时间
   *
   * @param group 组
   * @param key   键
   * @param time  时间(秒)
   */
  boolean expire(String group, String key, int time);

  /**
   * 根据key 获取过期时间
   *
   * @param group 组
   * @param key   键 不能为null
   * @return 时间(秒) 返回0代表为永久有效
   */
  long getExpire(String group, String key);

  /**
   * 判断key是否存在
   *
   * @param group 组
   * @param key   键
   * @return true 存在 false不存在
   */
  boolean hasKey(String group, String key);

  /**
   * 删除缓存
   *
   * @param key 可以传一个值 或多个
   */
  void del(String group, String... key);

  /**
   * 普通缓存获取
   *
   * @param group 组
   * @param key   键
   * @return 值
   */
  <T> T get(String group, String key);

  /**
   * 普通缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   * @return true成功 false失败
   */
  <T> T set(String group, String key, T value);

  /**
   * 普通缓存放入并设置时间
   *
   * @param group 组
   * @param key   键
   * @param value 值
   * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
   * @return true成功 false 失败
   */
  <T> T set(String group, String key, T value, int time);

  /**
   * 无序集合缓存获取
   *
   * @param group 组
   * @param key   键
   * @return 值
   */
  <T extends Set> T setGet(String group, String key);

  /**
   * 无序集合缓存放入并设置时间
   *
   * @param group 组
   * @param key   键
   * @param value 值
   * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
   * @return true成功 false 失败
   */
  <T extends Set> T set(String group, String key, int time, T... value);

  /**
   * 无序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   * @return true成功 false 失败
   */
  <T extends Set> T set(String group, String key, T... value);

  /**
   * 无序集合缓删除
   *
   * @param group 组
   * @param key   键
   * @param value 值
   * @return true成功 false 失败
   */
  Long remove(String group, String key, Object... value);

  /**
   * 有序集合缓存获取
   *
   * @param group 组
   * @param key   键
   * @param start 开始页
   * @param end 结束页
   * @return 值
   */
  <T> T ListGet(String group, String key,int start,int end);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T leftPush(String group,String key,T value, int time);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T leftPush(String group,String key,T value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T leftPushAll(String group,String key, int time,T... value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T leftPushAll(String group,String key, int time,List<T> value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T leftPushAll(String group,String key,T... value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T leftPushAll(String group,String key,List<T> value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T rightPush(String group,String key,T value, int time);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T rightPushAll(String group,String key, int time,T... value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T rightPushAll(String group,String key, int time,List<T> value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T rightPushAll(String group,String key,T... value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T rightPushAll(String group,String key,List<T> value);

  /**
   * 有序集合缓存放入
   *
   * @param group 组
   * @param key   键
   * @param value 值
   */
  <T> T rightPush(String group,String key,T value);

  /**
   * 哈希缓存获取
   *
   * @param group 组
   * @param key   键
   * @return 值
   */
  <T> T hashGet(String group, String key,String hashKey);

  /**
   * 哈希缓存设值
   * @param group 组
   * @param key   键
   * @param hashKey 哈希Key
   * @param value 值
   * @param <T>
   * @return
   */
  <T> T put(String group, String key,String hashKey,T value);

  /**
   * 哈希缓存设值
   * @param group 组
   * @param key   键
   * @param hashKey 哈希Key
   * @param value 值
   * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
   * @return
   */
  <T> T put(String group, String key,String hashKey,T value, int time);

  /**
   * 哈希缓存设值
   * @param group 组
   * @param key   键
   * @param value 值
   * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
   * @return
   */
  <T extends Map> T putAll(String group, String key,T value, int time);

  /**
   * 哈希缓存设值
   * @param group 组
   * @param key   键
   * @param value 值
   * @return
   */
  <T extends Map> T putAll(String group, String key,T value);

  /**
   * 哈希缓存设值
   * @param group 组
   * @param key   键
   * @param hashKey 哈希值
   * @return
   */
   Long delete(String group, String key,Object... hashKey);

  /**
   * 设置setnx的锁
   *
   * @param group 组
   * @param key   键
   * @param value 值
   * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
   * @return true没有锁，但加锁成功 false 已存在锁，加锁失败失败
   */
  boolean lockSet(String group, String key, Object value, int time);

  /**
   * 删除锁
   *
   * @param group 组
   * @param key   键
   */
  void deleteLock(String group, String key);
}
