package com.steven.solomon.cache.redis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.steven.solomon.cache.redis.context.RedisCacheTenantContext;
import com.steven.solomon.context.TenantRequestBinder;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * {@link RedisCacheAutoConfiguration#redisCacheTenantRequestBinder} 单元测试。
 *
 * <p>验证租户请求绑定器的 bind 和 clear 功能，
 * 确保绑定后能从上下文中获取对应租户的连接工厂。</p>
 */
class RedisCacheTenantRequestBinderTest {

  /**
   * 验证 bind 后上下文能获取对应的连接工厂，clear 后恢复为 null。
   */
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
