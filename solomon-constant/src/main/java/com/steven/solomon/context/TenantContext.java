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
  public F getFactory() {
    return THREAD_LOCAL.get();
  }

  /**
   * 设置工厂
   * @param key
   */
  public void setFactory(String key) {
    THREAD_LOCAL.set(FACTORY_MAP.get(key));
  }

  /**
   * 删除工厂
   */
  public void removeFactory() {
    THREAD_LOCAL.remove();
  }

  /**
   * 获取所有工厂
   * @return
   */
  public Map<String, F> getFactoryMap() {
    return FACTORY_MAP;
  }

  /**
   * set工厂
   * @param factoryMap
   */
  public void setFactory(Map<String, F> factoryMap) {
    FACTORY_MAP.putAll(factoryMap);
  }

  /**
   * set工厂
   * @param key
   * @param factory
   */
  public void setFactory(String key, F factory) {
    FACTORY_MAP.put(key,factory);
  }
}
