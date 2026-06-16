package com.steven.solomon.persistence.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.persistence.routing.DataSourceTenantContext;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class DataSourceTenantRequestBinderTest {

  @Test
  void bindAndClearTenantDataSource() throws Exception {
    DataSource tenantDataSource = mock(DataSource.class);
    DataSourceTenantContext context = new DataSourceTenantContext();
    context.registerFactory("tenant-1", tenantDataSource);
    TenantRequestBinder binder =
        new PersistenceAutoConfiguration().dataSourceTenantRequestBinder(context);

    binder.bind("tenant-1");

    assertEquals("tenant-1", context.getCurrentTenantCode());
    assertEquals(tenantDataSource, context.getFactory());

    binder.clear();

    assertNull(context.getCurrentTenantCode());
    assertNull(context.getFactory());
  }
}
