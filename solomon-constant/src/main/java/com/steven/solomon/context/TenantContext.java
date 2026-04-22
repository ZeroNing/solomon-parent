package com.steven.solomon.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 租户上下文抽象类
 * <p>用于管理多租户环境下的数据源/缓存工厂</p>
 * 
 * <b>⚠️ 重要警告：</b>使用setFactory后必须在finally块中调用removeFactory()，
 * 否则会导致内存泄漏！建议使用trySetFactory方法自动清理。</p>
 *
 * @param <F> 工厂类型（如DataSource, RedisConnectionFactory等）
 * @author steven
 * @since 1.0.0
 */
public abstract class TenantContext<F> {

  /**
   * 线程本地工厂存储
   * <p>⚠️ 必须在finally块中调用remove()清理，否则会内存泄漏</p>
   */
  protected ThreadLocal<F> threadLocal = new ThreadLocal<>();

  /**
   * 租户工厂映射表
   */
  protected final Map<String, F> factoryMap = new ConcurrentHashMap<>();

  /**
   * 获取当前线程的工厂
   *
   * @return 当前线程绑定的工厂，可能为null
   */
  public F getFactory() {
    return threadLocal.get();
  }

  /**
   * 设置当前线程的工厂
   * <p>⚠️ 警告：调用此方法后必须在finally块中调用removeFactory()清理，否则会内存泄漏！</p>
   * <p>推荐使用{@link #trySetFactory(String, Runnable)}自动清理</p>
   *
   * @param tenantId 租户编码
   * @see #trySetFactory(String, Runnable)
   */
  public void setFactory(String tenantId) {
    F factory = factoryMap.get(tenantId);
    if (factory == null) {
      throw new IllegalStateException("未找到租户[" + tenantId + "]对应的工厂，请先注册");
    }
    threadLocal.set(factory);
  }

  /**
   * 安全设置工厂（自动清理）
   * <p>使用try-finally模式自动清理ThreadLocal，避免内存泄漏</p>
   *
   * <pre>{@code
   * tenantContext.trySetFactory("tenant-001", () -> {
   *     // 业务逻辑
   *     doSomething();
   * });
   * }</pre>
   *
   * @param tenantId 租户编码
   * @param task 在租户上下文中执行的任务
   * @throws NullPointerException 如果task为null
   * @throws IllegalStateException 如果租户未注册
   */
  public void trySetFactory(String tenantId, Runnable task) {
    if (task == null) {
      throw new NullPointerException("task不能为null");
    }
    try {
      setFactory(tenantId);
      task.run();
    } finally {
      removeFactory();
    }
  }

  /**
   * 清理当前线程的工厂
   * <p>⚠️ 必须在finally块中调用，否则会内存泄漏</p>
   */
  public void removeFactory() {
    threadLocal.remove();
  }

  /**
   * 获取所有已注册的工厂
   *
   * @return 工厂映射表（不可修改）
   */
  public Map<String, F> getFactoryMap() {
    return Map.copyOf(factoryMap);
  }

  /**
   * 批量注册工厂
   *
   * @param factories 工厂映射表
   * @throws NullPointerException 如果factories为null
   */
  public synchronized void registerFactories(Map<String, F> factories) {
    if (factories == null) {
      throw new NullPointerException("factories不能为null");
    }
    factoryMap.putAll(factories);
  }

  /**
   * 注册单个工厂
   *
   * @param tenantId 租户编码
   * @param factory 工厂实例
   * @throws NullPointerException 如果参数为null
   */
  public void registerFactory(String tenantId, F factory) {
    if (tenantId == null || factory == null) {
      throw new NullPointerException("tenantId和factory不能为null");
    }
    factoryMap.put(tenantId, factory);
  }

  /**
   * 移除指定租户的工厂
   *
   * @param tenantId 租户编码
   * @return 被移除的工厂，如果不存在则返回null
   */
  public F unregisterFactory(String tenantId) {
    return factoryMap.remove(tenantId);
  }

  /**
   * 检查租户是否已注册
   *
   * @param tenantId 租户编码
   * @return 是否已注册
   */
  public boolean isRegistered(String tenantId) {
    return factoryMap.containsKey(tenantId);
  }
}
