package com.steven.solomon.cache.key;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * {@link CacheKeyBuilder} 单元测试。
 *
 * <p>验证四种缓存 key 组装模式下的 key 生成结果是否正确。</p>
 */
class CacheKeyBuilderTest {

  /**
   * NONE 模式：直接返回原始 key，不加任何前缀。
   */
  @Test
  void noneModeReturnsRawKey() {
    String key = CacheKeys.none().build("user", "10001");

    assertEquals("10001", key);
  }

  /**
   * PREFIX 模式：拼接全局前缀、分组和 key。
   */
  @Test
  void prefixModeAddsGlobalPrefixAndGroup() {
    String key = CacheKeys.prefix("app").build("user", "10001");

    assertEquals("app:user:10001", key);
  }

  /**
   * TENANT_SWITCH 模式：只加全局前缀和分组，不拼租户编码。
   */
  @Test
  void tenantSwitchModeDoesNotAddTenantCode() {
    String key = CacheKeys.tenantSwitch("app").build("user", "10001");

    assertEquals("app:user:10001", key);
  }
}
