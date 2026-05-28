package com.steven.solomon.service;

import com.steven.solomon.service.AbsICacheService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存服务空实现（占位/降级用）。
 *
 * <p>所有方法均返回空值或空操作，用于未配置具体缓存实现时的降级处理。</p>
 */
public class DefaultService extends AbsICacheService {

  /**
   * {@inheritDoc} 空实现，不执行任何操作。
   */
  @Override
  public void expire(String group, String key, int time) {
  }

  /**
   * {@inheritDoc} 空实现，返回null。
   */
  @Override
  public Long getExpire(String group, String key) {
    return null;
  }

  /**
   * {@inheritDoc} 空实现，返回false。
   */
  @Override
  public boolean hasKey(String group, String key) {
    return false;
  }

  /**
   * {@inheritDoc} 空实现，不执行任何操作。
   */
  @Override
  public void del(String group, String... key) {

  }

  /**
   * {@inheritDoc} 空实现，返回null。
   */
  @Override
  public <T> T get(String group, String key) {
    return null;
  }

  /**
   * {@inheritDoc} 空实现，返回null。
   */
  @Override
  public <T> T set(String group, String key, T value) {
    return null;
  }

  /**
   * {@inheritDoc} 空实现，返回null。
   */
  @Override
  public <T> T set(String group, String key, T value, int time) {
    return null;
  }

  /**
   * {@inheritDoc} 空实现，返回null。
   */
  @Override
  public Long delete(String group, String key, Object... hashKey) {
    return null;
  }

  /**
   * {@inheritDoc} 空实现，返回false。
   */
  @Override
  public boolean lockSet(String group, String key, Object value, int time) {
    return false;
  }

  /**
   * {@inheritDoc} 空实现，不执行任何操作。
   */
  @Override
  public void deleteLock(String group, String key) {

  }
}
