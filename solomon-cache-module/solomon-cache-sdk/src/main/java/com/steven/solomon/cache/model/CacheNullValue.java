package com.steven.solomon.cache.model;

import java.io.Serializable;

/**
 * 缓存空值占位对象，避免缓存穿透时反复访问数据库。
 */
public class CacheNullValue implements Serializable {

  private static final long serialVersionUID = 1L;

  private String value = "NULL";

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
