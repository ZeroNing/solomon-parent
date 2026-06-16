package com.steven.solomon.persistence.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.steven.solomon.persistence.enums.DataSourcePoolTypeEnum;
import com.steven.solomon.persistence.properties.TenantDataSourceProperties;
import org.junit.jupiter.api.Test;

class DynamicDataSourceFactoryTest {

  private final DynamicDataSourceFactory factory = new DynamicDataSourceFactory();

  @Test
  void shouldCreateIndependentDruidMonitorForEveryTenant() throws Exception {
    DruidDataSource first = createDruid("jdbc:mysql://127.0.0.1:3306/tenant_a", 1000);
    DruidDataSource second = createDruid("jdbc:mysql://127.0.0.1:3306/tenant_b", 5000);
    try {
      StatFilter firstFilter = (StatFilter) first.getProxyFilters().get(0);
      StatFilter secondFilter = (StatFilter) second.getProxyFilters().get(0);
      assertNotSame(firstFilter, secondFilter);
      assertEquals(1000, firstFilter.getSlowSqlMillis());
      assertEquals(5000, secondFilter.getSlowSqlMillis());
    } finally {
      first.close();
      second.close();
    }
  }

  private DruidDataSource createDruid(String url, long slowSqlMillis) throws Exception {
    TenantDataSourceProperties properties = new TenantDataSourceProperties();
    properties.setPoolType(DataSourcePoolTypeEnum.DRUID);
    properties.setUrl(url);
    properties.getMonitor().setEnabled(true);
    properties.getMonitor().setSlowSqlMillis(slowSqlMillis);
    return (DruidDataSource) factory.createDataSource(properties);
  }
}
