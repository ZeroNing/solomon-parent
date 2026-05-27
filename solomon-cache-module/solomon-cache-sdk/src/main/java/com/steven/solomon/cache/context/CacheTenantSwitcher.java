package com.steven.solomon.cache.context;

import java.util.function.Supplier;

/**
 * 缓存租户切换工具，提供更清晰的业务入口。
 */
public class CacheTenantSwitcher<R> {

  private final CacheTenantContext<R> tenantContext;

  public CacheTenantSwitcher(CacheTenantContext<R> tenantContext) {
    if (tenantContext == null) {
      throw new IllegalArgumentException("tenantContext 不能为空");
    }
    this.tenantContext = tenantContext;
  }

  public CacheTenantSwitcher<R> switchTo(String tenantCode) {
    tenantContext.switchTo(tenantCode);
    return this;
  }

  public CacheTenantSwitcher<R> clear() {
    tenantContext.clear();
    return this;
  }

  public void run(String tenantCode, Runnable task) {
    tenantContext.run(tenantCode, task);
  }

  public <T> T execute(String tenantCode, Supplier<T> supplier) {
    return tenantContext.execute(tenantCode, supplier);
  }
}
