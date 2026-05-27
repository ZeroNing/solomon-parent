package com.steven.solomon.cache.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CacheDigestUtilsTest {

  @Test
  void sha256ReturnsStableDigest() {
    assertEquals(
        "5e1ecee06a7fc06f305ae5c12acfe7a7f67b8ece7af76932ed3afab00c3c6921",
        CacheDigestUtils.sha256("cache"));
  }
}
