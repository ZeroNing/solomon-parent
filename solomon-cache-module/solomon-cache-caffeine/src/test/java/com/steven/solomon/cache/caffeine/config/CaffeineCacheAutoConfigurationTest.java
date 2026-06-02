package com.steven.solomon.cache.caffeine.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.cache.caffeine.properties.CaffeineCacheProperties;
import com.steven.solomon.cache.key.CacheKeyMode;
import org.junit.jupiter.api.Test;

/**
 * {@link CaffeineCacheAutoConfiguration} 单元测试。
 *
 * <p>验证 Caffeine 缓存组件的配置校验逻辑，
 * 确保 TENANT_SWITCH 模式被正确拒绝，TENANT_PREFIX 模式正常允许。</p>
 */
class CaffeineCacheAutoConfigurationTest {

  private final CaffeineCacheAutoConfiguration configuration =
      new CaffeineCacheAutoConfiguration();

  /**
   * 验证 Caffeine 不支持 TENANT_SWITCH 模式。
   */
  @Test
  void rejectTenantSwitchMode() {
    CaffeineCacheProperties properties = properties(CacheKeyMode.TENANT_SWITCH);

    assertThrows(IllegalStateException.class,
        () -> configuration.caffeineCacheModeValidator(properties).afterPropertiesSet());
  }

  /**
   * 验证 Caffeine 支持 TENANT_PREFIX 模式。
   */
  @Test
  void allowTenantPrefixMode() {
    CaffeineCacheProperties properties = properties(CacheKeyMode.TENANT_PREFIX);

    assertDoesNotThrow(
        () -> configuration.caffeineCacheModeValidator(properties).afterPropertiesSet());
  }

  /**
   * 创建指定模式的测试属性。
   */
  private CaffeineCacheProperties properties(CacheKeyMode mode) {
    CaffeineCacheProperties properties = new CaffeineCacheProperties();
    properties.getKey().setMode(mode);
    return properties;
  }
}
