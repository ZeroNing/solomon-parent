package com.steven.solomon.cache.redis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.steven.solomon.cache.redis.context.RedisCacheTenantContext;
import com.steven.solomon.context.TenantRequestBinder;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;

class RedisCacheTenantRequestBinderTest {

  @Test
  void bindAndClearTenantRedisConnectionFactory() throws Exception {
    RedisConnectionFactory tenantFactory = mock(RedisConnectionFactory.class);
    RedisCacheTenantContext context = new RedisCacheTenantContext();
    context.registerFactory("tenant-1", tenantFactory);
    TenantRequestBinder binder =
        new RedisCacheAutoConfiguration().redisCacheTenantRequestBinder(context);

    binder.bind("tenant-1");

    assertEquals(tenantFactory, context.getFactory());

    binder.clear();

    assertNull(context.getFactory());
  }
}
