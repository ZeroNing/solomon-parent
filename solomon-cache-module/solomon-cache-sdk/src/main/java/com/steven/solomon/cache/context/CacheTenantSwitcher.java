package com.steven.solomon.cache.context;

import com.steven.solomon.context.TenantContext;
import java.util.function.Supplier;

/**
 * 缓存租户切换工具。
 *
 * <p>封装 {@link TenantContext} 提供面向缓存业务的租户切换 API，
 * 支持切换、清除、执行任务和带返回值的执行等操作。</p>
 *
 * @param <R> 租户资源类型
 */
public class CacheTenantSwitcher<R> {

  /** 原始租户上下文。 */
  private final TenantContext<R> tenantContext;

  public CacheTenantSwitcher(TenantContext<R> tenantContext) {
    if (tenantContext == null) {
      throw new IllegalArgumentException("tenantContext 不能为空");
    }
    this.tenantContext = tenantContext;
  }

  /**
   * 切换到指定租户。
   *
   * @param tenantCode 租户编码
   * @return 当前实例（链式调用）
   */
  public CacheTenantSwitcher<R> switchTo(String tenantCode) {
    tenantContext.setFactory(tenantCode);
    return this;
  }

  /**
   * 清除当前租户切换状态，恢复默认。
   */
  public CacheTenantSwitcher<R> clear() {
    tenantContext.removeFactory();
    return this;
  }

  /**
   * 在指定租户下执行无返回值的任务。
   *
   * @param tenantCode 租户编码
   * @param task       要执行的任务
   */
  public void run(String tenantCode, Runnable task) {
    tenantContext.trySetFactory(tenantCode, task);
  }

  /**
   * 在指定租户下执行有返回值的任务。
   *
   * @param tenantCode 租户编码
   * @param supplier   要执行的任务
   * @param <T>        返回值类型
   * @return 任务执行结果
   */
  public <T> T execute(String tenantCode, Supplier<T> supplier) {
    return tenantContext.executeWithFactory(tenantCode, supplier);
  }
}
