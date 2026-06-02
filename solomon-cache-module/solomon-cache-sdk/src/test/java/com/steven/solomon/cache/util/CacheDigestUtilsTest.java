package com.steven.solomon.cache.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * {@link CacheDigestUtils} 单元测试。
 *
 * <p>验证 SHA-256 摘要的稳定性和正确性。</p>
 */
class CacheDigestUtilsTest {

  /**
   * 验证相同输入产生固定输出，确保摘要可重复。
   */
  @Test
  void sha256ReturnsStableDigest() {
    assertEquals(
        "5e1ecee06a7fc06f305ae5c12acfe7a7f67b8ece7af76932ed3afab00c3c6921",
        CacheDigestUtils.sha256("cache"));
  }
}
