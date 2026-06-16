package com.steven.solomon.persistence.config;

import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.persistence.exception.PersistenceException;
import com.steven.solomon.persistence.routing.DataSourceTenantContext;

/**
 * 将网关透传的租户编码绑定到当前线程的数据源上下文�? */
public class DataSourceTenantRequestBinder implements TenantRequestBinder {

  private final DataSourceTenantContext context;

  public DataSourceTenantRequestBinder(DataSourceTenantContext context) {
    this.context = context;
  }

  @Override
  public void bind(String tenantCode) throws PersistenceException {
    context.switchTenant(tenantCode);
  }

  @Override
  public void clear() {
    context.removeFactory();
  }
}
