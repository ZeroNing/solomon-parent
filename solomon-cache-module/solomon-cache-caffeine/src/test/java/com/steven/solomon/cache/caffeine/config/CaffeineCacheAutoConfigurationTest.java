package com.steven.solomon.cache.caffeine.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.cache.caffeine.properties.CaffeineCacheProperties;
import com.steven.solomon.cache.key.CacheKeyMode;
import org.junit.jupiter.api.Test;

class CaffeineCacheAutoConfigurationTest {

  private final CaffeineCacheAutoConfiguration configuration =
      new CaffeineCacheAutoConfiguration();

  @Test
  void rejectTenantSwitchMode() {
    CaffeineCacheProperties properties = properties(CacheKeyMode.TENANT_SWITCH);

    assertThrows(IllegalStateException.class,
        () -> configuration.caffeineCacheModeValidator(properties).afterPropertiesSet());
  }

  @Test
  void allowTenantPrefixMode() {
    CaffeineCacheProperties properties = properties(CacheKeyMode.TENANT_PREFIX);

    assertDoesNotThrow(
        () -> configuration.caffeineCacheModeValidator(properties).afterPropertiesSet());
  }

  private CaffeineCacheProperties properties(CacheKeyMode mode) {
    CaffeineCacheProperties properties = new CaffeineCacheProperties();
    properties.getKey().setMode(mode);
    return properties;
  }
}
