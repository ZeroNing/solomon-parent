package com.steven.solomon.context;

/**
 * HTTP 请求租户资源绑定扩展点。
 *
 * <p>下游模块实现此接口后，可在请求进入时按租户编码切换资源，并在请求结束时可靠清理。
 * 数据源、Redis、MongoDB 等租户资源均可复用此生命周期。</p>
 */
public interface TenantRequestBinder {

  /** 绑定当前请求的租户资源。 */
  void bind(String tenantCode) throws Exception;

  /** 清理当前线程绑定的租户资源。 */
  void clear();
}
