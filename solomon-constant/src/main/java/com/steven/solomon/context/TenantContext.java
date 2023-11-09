package com.steven.solomon.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TenantContext<F> {

  protected ThreadLocal<F> THREAD_LOCAL = new ThreadLocal<>();

  protected Map<String, F> FACTORY_MAP = new ConcurrentHashMap<>();

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
}
