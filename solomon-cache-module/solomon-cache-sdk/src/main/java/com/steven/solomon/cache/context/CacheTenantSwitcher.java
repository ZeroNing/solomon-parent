package com.steven.solomon.cache.context;

import com.steven.solomon.context.TenantContext;
import java.util.function.Supplier;

/**
 * 缓存租户切换工具，提供更清晰的业务入口。
 */
public class CacheTenantSwitcher<R> {

  private final TenantContext<R> tenantContext;

  public CacheTenantSwitcher(TenantContext<R> tenantContext) {
    if (tenantContext == null) {
      throw new IllegalArgumentException("tenantContext 不能为空");
    }
    this.tenantContext = tenantContext;
  }

  public CacheTenantSwitcher<R> switchTo(String tenantCode) {
    tenantContext.setFactory(tenantCode);
    return this;
  }

  public CacheTenantSwitcher<R> clear() {
    tenantContext.removeFactory();
    return this;
  }

  public void run(String tenantCode, Runnable task) {
    tenantContext.trySetFactory(tenantCode, task);
  }

  public <T> T execute(String tenantCode, Supplier<T> supplier) {
    return tenantContext.executeWithFactory(tenantCode, supplier);
  }
}
