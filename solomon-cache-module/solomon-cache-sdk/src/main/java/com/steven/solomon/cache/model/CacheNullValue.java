package com.steven.solomon.cache.model;

import java.io.Serializable;

/**
 * 缓存空值占位对象。
 *
 * <p>当方法返回 null 但 {@link com.steven.solomon.cache.annotation.CacheResult#cacheNull()} 为 true 时，
 * 使用此对象替代 null 存入缓存，以便从缓存中区分「缓存了 null」和「缓存未命中」。
 * 实现 {@link Serializable} 接口以支持 Redis 等序列化缓存。</p>
 */
public class CacheNullValue implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 空值标记。 */
  private String value = "NULL";

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
