package com.steven.solomon.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TenantContext<F,P> {

  private Map<String,F> factoryMap = new ConcurrentHashMap<>();

  private Map<String,P> propertiesMap = new ConcurrentHashMap<>();

  /**
   * 获取工厂
   * @return
   */
  public abstract F getFactory();

  /**
   * 设置工厂
   * @param key
   */
  public abstract void setFactory(String key);

  /**
   * 删除工厂
   */
  public abstract void removeFactory();

  /**
   * 获取所有工厂
   * @return
   */
  public abstract Map<String,F> getFactoryMap();

  /**
   * set工厂
   * @param factoryMap
   */
  public abstract void setFactory(Map<String, F> factoryMap);

  /**
   * set工厂
   * @param key
   * @param factory
   */
  public abstract void setFactory(String key,F factory);

  /**
   * 获取全部配置
   */
  public abstract Map<String,P> getPropertiesMap();

  /**
   * 设置配置
   */
  public abstract void setProperties(String key,P properties);

  /**
   * 设置配置
   */
  public abstract void setProperties(Map<String, P> propertiesMap);
}
