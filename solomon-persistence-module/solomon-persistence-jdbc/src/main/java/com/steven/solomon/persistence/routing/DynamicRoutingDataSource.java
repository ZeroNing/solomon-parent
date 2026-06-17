package com.steven.solomon.persistence.routing;

import cn.hutool.core.util.ObjectUtil;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Spring JDBC动态路由数据源。
 *
 * <p>Spring在获取连接时会进入这里，优先返回当前线程绑定的数据源；未切换时返回默认数据源。</p>
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

  private final DataSourceTenantContext tenantContext;

  /**
   * 构造动态路由数据源。
   *
   * @param tenantContext 数据源租户上下文，用于获取当前线程绑定的数据源
   */
  public DynamicRoutingDataSource(DataSourceTenantContext tenantContext) {
    this.tenantContext = tenantContext;
  }

  /**
   * 获取当前路由键。
   *
   * @return 当前租户编码；未切换时返回unknown
   */
  @Override
  protected Object determineCurrentLookupKey() {
    return tenantContext.getCurrentTenantId();
  }

  /**
   * 获取当前线程实际使用的数据源。
   *
   * @return 当前线程绑定的数据源；未绑定时返回默认数据源
   */
  @Override
  protected DataSource determineTargetDataSource() {
    DataSource dataSource = tenantContext.getFactory();
    return ObjectUtil.isEmpty(dataSource) ? super.determineTargetDataSource() : dataSource;
  }
}
