package com.steven.solomon.service;


public interface ICacheService {

  /**
   * 指定缓存失效时间
   *
   * @param group 组
   * @param key   键
   * @param time  时间(秒)
   */
  void expire(String group, String key, int time);

  /**
   * 根据key 获取过期时间
   *
   * @param group 组
   * @param key   键 不能为null
   * @return 时间(秒) 返回0代表为永久有效
   */
  Long getExpire(String group, String key);

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
   * 哈希缓存设值
   * @param group 组
   * @param key   键
   * @param hashKey 哈希值
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
