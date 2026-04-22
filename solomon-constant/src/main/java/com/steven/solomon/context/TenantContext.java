package com.steven.solomon.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 租户上下文抽象类
 * <p>用于管理多租户环境下的数据源/缓存工厂，支持线程安全的租户切换</p>
 * 
 * <h2>⚠️ 内存泄漏警告</h2>
 * <p>使用setFactory后必须在finally块中调用removeFactory()，
 * 否则会导致ThreadLocal内存泄漏！建议使用trySetFactory方法自动清理。</p>
 * 
 * <h2>核心设计</h2>
 * <ul>
 *   <li>使用{@link ThreadLocal}实现线程隔离</li>
 *   <li>使用{@link ConcurrentHashMap}存储租户工厂映射</li>
 *   <li>支持动态注册/注销租户工厂</li>
 * </ul>
 *
 * @param <F> 工厂类型（如DataSource, RedisConnectionFactory等）
 * @author steven
 * @since 1.0.0
 * @see ThreadLocal
 */
public abstract class TenantContext<F> {

  /**
   * 日志记录器
   * <p>用于记录租户上下文的创建、切换、清理等关键操作</p>
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * 线程本地工厂存储
   * <p>⚠️ 必须在finally块中调用remove()清理，否则会内存泄漏</p>
   * <p>每个线程独立持有自己的工厂实例，实现租户隔离</p>
   */
  protected ThreadLocal<F> threadLocal = new ThreadLocal<>();

  /**
   * 租户工厂映射表
   * <p>线程安全的映射表，存储所有租户的工厂实例</p>
   * <p>Key: 租户编码，Value: 工厂实例</p>
   */
  protected final Map<String, F> factoryMap = new ConcurrentHashMap<>();

  /**
   * 获取当前线程的工厂
   *
   * @return 当前线程绑定的工厂，可能为null
   */
  public F getFactory() {
    F factory = threadLocal.get();
    // Debug级别日志：频繁调用，避免性能影响
    if (logger.isDebugEnabled()) {
      logger.debug("[TenantContext] 获取当前线程工厂: tenantId={}, hasFactory={}", 
          getCurrentTenantId(), factory != null);
    }
    return factory;
  }

  /**
   * 获取当前租户编码（子类可覆写）
   *
   * @return 当前租户编码，默认返回"unknown"
   */
  protected String getCurrentTenantId() {
    return "unknown";
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
      // 错误日志：租户未注册是异常情况，需要排查
      logger.error("[TenantContext] 租户未注册: tenantId={}, 已注册租户={}", 
          tenantId, factoryMap.keySet());
      throw new IllegalStateException("未找到租户[" + tenantId + "]对应的工厂，请先注册");
    }
    threadLocal.set(factory);
    // Info级别日志：租户切换是关键操作
    logger.info("[TenantContext] 切换租户上下文: tenantId={}", tenantId);
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
      logger.warn("[TenantContext] trySetFactory参数为null: tenantId={}", tenantId);
      throw new NullPointerException("task不能为null");
    }
    
    logger.debug("[TenantContext] 开始执行租户任务: tenantId={}", tenantId);
    try {
      setFactory(tenantId);
      task.run();
      logger.debug("[TenantContext] 租户任务执行完成: tenantId={}", tenantId);
    } catch (Exception e) {
      // 错误日志：任务执行异常
      logger.error("[TenantContext] 租户任务执行异常: tenantId={}, error={}", 
          tenantId, e.getMessage(), e);
      throw e;
    } finally {
      removeFactory();
      logger.debug("[TenantContext] 已清理租户上下文: tenantId={}", tenantId);
    }
  }

  /**
   * 清理当前线程的工厂
   * <p>⚠️ 必须在finally块中调用，否则会内存泄漏</p>
   */
  public void removeFactory() {
    threadLocal.remove();
    // Debug级别日志：频繁调用
    logger.debug("[TenantContext] 已清理当前线程的工厂");
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
      logger.warn("[TenantContext] registerFactories参数为null");
      throw new NullPointerException("factories不能为null");
    }
    factoryMap.putAll(factories);
    // Info级别日志：批量注册是关键操作
    logger.info("[TenantContext] 批量注册工厂: count={}, tenants={}", 
        factories.size(), factories.keySet());
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
      logger.warn("[TenantContext] registerFactory参数为null: tenantId={}, factory={}", 
          tenantId, factory != null);
      throw new NullPointerException("tenantId和factory不能为null");
    }
    factoryMap.put(tenantId, factory);
    // Info级别日志：工厂注册是关键操作
    logger.info("[TenantContext] 注册工厂: tenantId={}, total={}", tenantId, factoryMap.size());
  }

  /**
   * 移除指定租户的工厂
   *
   * @param tenantId 租户编码
   * @return 被移除的工厂，如果不存在则返回null
   */
  public F unregisterFactory(String tenantId) {
    F removed = factoryMap.remove(tenantId);
    // Info级别日志：工厂注销是关键操作
    if (removed != null) {
      logger.info("[TenantContext] 注销工厂: tenantId={}, remaining={}", tenantId, factoryMap.size());
    } else {
      logger.warn("[TenantContext] 注销工厂失败，租户不存在: tenantId={}", tenantId);
    }
    return removed;
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
