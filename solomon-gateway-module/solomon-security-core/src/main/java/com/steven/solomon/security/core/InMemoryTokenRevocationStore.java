package com.steven.solomon.security.core;

import cn.hutool.core.util.StrUtil;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenRevocationStore implements TokenRevocationStore {

  private final Map<String, Long> revokedTokenExpiresAt = new ConcurrentHashMap<>();

  @Override
  public void revoke(String tokenId, long expiresAtMillis) {
    if (StrUtil.isBlank(tokenId)) {
      return;
    }
    cleanupExpired(System.currentTimeMillis());
    revokedTokenExpiresAt.put(StrUtil.trim(tokenId), expiresAtMillis);
  }

  @Override
  public boolean isRevoked(String tokenId) {
    if (StrUtil.isBlank(tokenId)) {
      return false;
    }
    long now = System.currentTimeMillis();
    cleanupExpired(now);
    Long expiresAt = revokedTokenExpiresAt.get(StrUtil.trim(tokenId));
    return expiresAt != null && expiresAt > now;
  }

  private void cleanupExpired(long now) {
    Iterator<Map.Entry<String, Long>> iterator = revokedTokenExpiresAt.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Long> entry = iterator.next();
      if (entry.getValue() <= now) {
        iterator.remove();
      }
    }
  }
}
