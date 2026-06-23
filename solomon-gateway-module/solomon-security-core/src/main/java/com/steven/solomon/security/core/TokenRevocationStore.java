package com.steven.solomon.security.core;

public interface TokenRevocationStore {

  void revoke(String tokenId, long expiresAtMillis);

  boolean isRevoked(String tokenId);
}
