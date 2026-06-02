package com.steven.solomon.gateway.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import org.junit.jupiter.api.Test;

class JwtTokenUtilsTest {

  @Test
  void createAndParseToken() {
    JwtTokenUtils tokenUtils = tokenUtils(7200);
    String token = tokenUtils.createToken(
        new GatewayTokenClaims("user-1", "tenant-1"));

    GatewayTokenClaims claims = tokenUtils.parseToken(token);

    assertEquals("user-1", claims.userId());
    assertEquals("tenant-1", claims.tenantCode());
  }

  @Test
  void rejectExpiredToken() {
    JwtTokenUtils tokenUtils = tokenUtils(-1);
    String token = tokenUtils.createToken(
        new GatewayTokenClaims("user-1", "tenant-1"));

    assertNull(tokenUtils.parseToken(token));
  }

  private JwtTokenUtils tokenUtils(long expireSeconds) {
    GatewayJwtProperties properties = new GatewayJwtProperties();
    properties.setSecret("gateway-test-secret");
    properties.setExpireSeconds(expireSeconds);
    return new JwtTokenUtils(properties);
  }
}
