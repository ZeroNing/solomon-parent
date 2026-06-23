package com.steven.solomon.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 租户资源作用域。
 *
 * <p>统一绑定数据源、Redis 等租户资源，并在作用域关闭时按相反顺序清理。
 * HTTP 请求、MQTT 消费等入口都可以复用，避免线程池复用时残留租户状态。</p>
 */
public final class TenantResourceScope implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantResourceScope.class);
  private static final List<TenantSwitchObserver> OBSERVERS = new CopyOnWriteArrayList<>();

  private final List<TenantRequestBinder> boundBinders = new ArrayList<>();
  private String tenantCode;

  private TenantResourceScope() {
  }

  /**
   * 打开租户资源作用域。
   *
   * @param tenantCode 租户编码
   * @param binders 租户资源绑定器
   * @return 可自动关闭的租户资源作用域
   */
  public static TenantResourceScope open(
      String tenantCode, Collection<TenantRequestBinder> binders) throws Exception {
    TenantResourceScope scope = new TenantResourceScope();
    scope.tenantCode = tenantCode;
    if (StrUtil.isBlank(tenantCode) || CollUtil.isEmpty(binders)) {
      return scope;
    }
    try {
      for (TenantRequestBinder binder : binders) {
        scope.boundBinders.add(binder);
        long startNanos = System.nanoTime();
        String binderName = binderName(binder);
        try {
          binder.bind(tenantCode);
          long durationNanos = System.nanoTime() - startNanos;
          notifyObservers(tenantCode, binderName, "bind", "success", durationNanos);
          LOGGER.debug("Tenant resource bind succeeded, tenant={}, binder={}, durationNanos={}",
              tenantCode, binderName, durationNanos);
        } catch (Exception ex) {
          long durationNanos = System.nanoTime() - startNanos;
          notifyObservers(tenantCode, binderName, "bind", "error", durationNanos);
          LOGGER.warn("Tenant resource bind failed, tenant={}, binder={}, durationNanos={}",
              tenantCode, binderName, durationNanos, ex);
          throw ex;
        }
      }
      return scope;
    } catch (Exception ex) {
      scope.close();
      throw ex;
    }
  }

  /** 按绑定顺序的相反方向清理租户资源。 */
  @Override
  public void close() {
    for (int index = boundBinders.size() - 1; index >= 0; index--) {
      TenantRequestBinder binder = boundBinders.get(index);
      long startNanos = System.nanoTime();
      String binderName = binderName(binder);
      try {
        binder.clear();
        long durationNanos = System.nanoTime() - startNanos;
        notifyObservers(tenantCode, binderName, "clear", "success", durationNanos);
        LOGGER.debug("Tenant resource clear succeeded, tenant={}, binder={}, durationNanos={}",
            tenantCode, binderName, durationNanos);
      } catch (RuntimeException ex) {
        long durationNanos = System.nanoTime() - startNanos;
        notifyObservers(tenantCode, binderName, "clear", "error", durationNanos);
        LOGGER.warn("Tenant resource clear failed, tenant={}, binder={}, durationNanos={}",
            tenantCode, binderName, durationNanos, ex);
        throw ex;
      }
    }
    boundBinders.clear();
  }

  public static void registerObserver(TenantSwitchObserver observer) {
    if (observer != null && !OBSERVERS.contains(observer)) {
      OBSERVERS.add(observer);
    }
  }

  public static void unregisterObserver(TenantSwitchObserver observer) {
    OBSERVERS.remove(observer);
  }

  static void clearObserversForTest() {
    OBSERVERS.clear();
  }

  private static void notifyObservers(
      String tenantCode, String binderName, String phase, String outcome, long durationNanos) {
    for (TenantSwitchObserver observer : OBSERVERS) {
      try {
        observer.record(tenantCode, binderName, phase, outcome, durationNanos);
      } catch (RuntimeException ex) {
        LOGGER.debug("Tenant switch observer failed, tenant={}, binder={}, phase={}, outcome={}",
            tenantCode, binderName, phase, outcome, ex);
      }
    }
  }

  private static String binderName(TenantRequestBinder binder) {
    return binder == null ? "unknown" : binder.getClass().getSimpleName();
  }
}
