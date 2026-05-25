package com.steven.solomon.context;

import cn.hutool.core.util.ObjectUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 多租户上下文基类。
 *
 * <p>该类负责维护“租户编码 -> 工厂对象”的映射，并通过 {@link ThreadLocal}
 * 为当前线程绑定一个工厂对象。典型场景包括动态数据源、动态 Redis、动态 MongoDB 等。</p>
 *
 * <p>使用 {@link #setFactory(String)} 后必须调用 {@link #removeFactory()}，
 * 否则线程池复用时可能把上一次请求的租户上下文带到下一次请求。
 * 推荐优先使用 {@link #trySetFactory(String, Runnable)}，它会自动清理上下文。</p>
 *
 * @param <F> 工厂对象类型，例如 DataSource、RedisConnectionFactory
 */
public abstract class TenantContext<F> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * 当前线程绑定的工厂对象。
   */
  private final ThreadLocal<F> threadLocal = new ThreadLocal<>();

  /**
   * 全局租户工厂表，线程安全，支持运行期注册和注销。
   */
  private final Map<String, F> factoryMap = new ConcurrentHashMap<>();

  /**
   * 获取当前线程绑定的工厂对象。
   *
   * @return 当前线程工厂对象，没有绑定时返回 null
   */
  public F getFactory() {
    return threadLocal.get();
  }

  /**
   * 将指定租户的工厂对象绑定到当前线程。
   *
   * @param tenantId 租户编码
   * @throws IllegalStateException 租户未注册时抛出
   */
  public void setFactory(String tenantId) {
    F factory = factoryMap.get(tenantId);
    if (ObjectUtil.isEmpty(factory)) {
      throw new IllegalStateException("未找到租户[" + tenantId + "]对应的工厂，请先注册");
    }
    threadLocal.set(factory);
    logger.debug("[TenantContext] 已切换租户上下文: tenantId={}", tenantId);
  }

  /**
   * 在指定租户上下文中执行任务，并在任务结束后自动清理 ThreadLocal。
   *
   * @param tenantId 租户编码
   * @param task 需要在租户上下文中执行的任务
   */
  public void trySetFactory(String tenantId, Runnable task) {
    requireNotEmpty(task, "task不能为null");
    try {
      setFactory(tenantId);
      task.run();
    } finally {
      removeFactory();
    }
  }

  /**
   * 清理当前线程的工厂对象。
   */
  public void removeFactory() {
    threadLocal.remove();
  }

  /**
   * 获取所有已注册工厂的只读快照。
   *
   * @return 租户工厂映射快照
   */
  public Map<String, F> getFactoryMap() {
    return Map.copyOf(factoryMap);
  }

  /**
   * 批量注册租户工厂。
   *
   * @param factories 租户工厂映射
   */
  public synchronized void registerFactories(Map<String, F> factories) {
    requireNotEmpty(factories, "factories不能为null");
    factoryMap.putAll(factories);
    logger.info("[TenantContext] 批量注册租户工厂: count={}", factories.size());
  }

  /**
   * 注册或覆盖单个租户工厂。
   *
   * @param tenantId 租户编码
   * @param factory 工厂对象
   */
  public void registerFactory(String tenantId, F factory) {
    requireNotEmpty(tenantId, "tenantId不能为null");
    requireNotEmpty(factory, "factory不能为null");
    factoryMap.put(tenantId, factory);
    logger.debug("[TenantContext] 已注册租户工厂: tenantId={}", tenantId);
  }

  /**
   * 注销指定租户工厂。
   *
   * @param tenantId 租户编码
   * @return 被移除的工厂对象，不存在时返回 null
   */
  public F unregisterFactory(String tenantId) {
    F removed = factoryMap.remove(tenantId);
    logger.debug("[TenantContext] 已注销租户工厂: tenantId={}, removed={}",
        tenantId, ObjectUtil.isNotEmpty(removed));
    return removed;
  }

  /**
   * 判断租户是否已经注册。
   *
   * @param tenantId 租户编码
   * @return true 表示已注册
   */
  public boolean isRegistered(String tenantId) {
    return factoryMap.containsKey(tenantId);
  }

  /**
   * 严格校验必填参数，统一使用 Hutool 判空，同时保留原有 NullPointerException 语义。
   */
  private void requireNotEmpty(Object value, String message) {
    if (ObjectUtil.isEmpty(value)) {
      throw new NullPointerException(message);
    }
  }
}
