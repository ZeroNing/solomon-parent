package com.steven.solomon.cache.key;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CacheKeyBuilderTest {

  @Test
  void noneModeReturnsRawKey() {
    String key = CacheKeys.none().build("user", "10001");

    assertEquals("10001", key);
  }

  @Test
  void prefixModeAddsGlobalPrefixAndGroup() {
    String key = CacheKeys.prefix("app").build("user", "10001");

    assertEquals("app:user:10001", key);
  }

  @Test
  void tenantSwitchModeDoesNotAddTenantCode() {
    String key = CacheKeys.tenantSwitch("app").build("user", "10001");

    assertEquals("app:user:10001", key);
  }
}
