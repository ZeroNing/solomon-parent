package com.steven.solomon.gateway.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenUtilsTest {

  private JwtTokenUtils tokenUtils;

  @BeforeEach
  void setUp() {
    GatewayJwtProperties properties = new GatewayJwtProperties();
    properties.setSecret("gateway-sdk-test-secret");
    tokenUtils = new JwtTokenUtils(properties);
  }

  @Test
  void generateAndParseToken() {
    String token = tokenUtils.generateToken(
        new GatewayTokenClaims("user-1", "tenant-a", Map.of("role", "admin")));

    assertTrue(tokenUtils.validateToken("Bearer " + token));
    assertEquals("user-1", tokenUtils.getSubject(token));
    assertEquals("tenant-a", tokenUtils.getTenantCode(token));
    assertEquals("admin", tokenUtils.getPayloads(token).get("role"));
  }

  @Test
  void rejectInvalidToken() {
    assertFalse(tokenUtils.validateToken("invalid-token"));
  }

  @Test
  void reservedClaimsCannotBeOverwritten() {
    String token = tokenUtils.generateToken(new GatewayTokenClaims(
        "user-1",
        "tenant-a",
        Map.of("sub", "other-user", "tenantCode", "other-tenant", "exp", 0)));

    assertEquals("user-1", tokenUtils.getSubject(token));
    assertEquals("tenant-a", tokenUtils.getTenantCode(token));
    assertTrue(tokenUtils.validateToken(token));
  }

  @Test
  void rejectWrongIssuer() {
    GatewayJwtProperties properties = new GatewayJwtProperties();
    properties.setSecret("gateway-sdk-test-secret");
    properties.setIssuer("other-issuer");
    JwtTokenUtils otherIssuer = new JwtTokenUtils(properties);
    String token = tokenUtils.generateToken(new GatewayTokenClaims("user-1", "tenant-a"));

    assertFalse(otherIssuer.validateToken(token));
  }
}
