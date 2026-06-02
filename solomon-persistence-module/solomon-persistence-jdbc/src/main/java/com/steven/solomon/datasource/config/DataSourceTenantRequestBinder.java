package com.steven.solomon.datasource.config;

import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.routing.DataSourceTenantContext;

/**
 * 将网关透传的租户编码绑定到当前线程的数据源上下文。
 */
public class DataSourceTenantRequestBinder implements TenantRequestBinder {

  private final DataSourceTenantContext context;

  public DataSourceTenantRequestBinder(DataSourceTenantContext context) {
    this.context = context;
  }

  @Override
  public void bind(String tenantCode) throws DataSourceException {
    context.switchTenant(tenantCode);
  }

  @Override
  public void clear() {
    context.removeFactory();
  }
}
