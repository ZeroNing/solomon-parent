package com.steven.solomon.datasource.routing;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Spring JDBC动态路由数据源。
 *
 * <p>Spring在获取连接时会进入这里，优先返回当前线程绑定的数据源；未切换时返回默认数据源。</p>
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

  private final DataSourceTenantContext tenantContext;

  public DynamicRoutingDataSource(DataSourceTenantContext tenantContext) {
    this.tenantContext = tenantContext;
  }

  @Override
  protected Object determineCurrentLookupKey() {
    return tenantContext.getCurrentTenantId();
  }

  @Override
  protected DataSource determineTargetDataSource() {
    DataSource dataSource = tenantContext.getFactory();
    return dataSource == null ? super.determineTargetDataSource() : dataSource;
  }
}
