package com.steven.solomon.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 租户资源作用域。
 *
 * <p>统一绑定数据源、Redis 等租户资源，并在作用域关闭时按相反顺序清理。
 * HTTP 请求、MQTT 消费等入口都可以复用，避免线程池复用时残留租户状态。</p>
 */
public final class TenantResourceScope implements AutoCloseable {

  private final List<TenantRequestBinder> boundBinders = new ArrayList<>();

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
    if (StrUtil.isBlank(tenantCode) || CollUtil.isEmpty(binders)) {
      return scope;
    }
    try {
      for (TenantRequestBinder binder : binders) {
        scope.boundBinders.add(binder);
        binder.bind(tenantCode);
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
      boundBinders.get(index).clear();
    }
    boundBinders.clear();
  }
}
