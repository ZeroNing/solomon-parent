package com.steven.solomon.security.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.security.model.SecurityTokenClaims;
import com.steven.solomon.security.model.SecurityUser;
import com.steven.solomon.security.properties.SecurityJwtProperties;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecurityTokenUtilsTest {

  private SecurityTokenUtils tokenUtils;

  @BeforeEach
  void setUp() {
    tokenUtils = new SecurityTokenUtils(properties("security-test-secret", "security-test"));
  }

  @Test
  void generateAndParseUser() {
    String token = tokenUtils.generateToken(new SecurityTokenClaims(
        "user-1",
        "tenant-a",
        "tenant-id",
        "租户 A",
        Set.of("order:read", "order:write"),
        Map.of("channel", "web")));

    SecurityUser user = tokenUtils.parseUser("Bearer " + token);

    assertEquals("user-1", user.getUsername());
    assertEquals("tenant-a", user.tenantCode());
    assertEquals("tenant-id", user.tenantId());
    assertEquals(2, user.getAuthorities().size());
    assertEquals("web", tokenUtils.getPayloads(token).get("channel"));
  }

  @Test
  void reservedClaimsCannotBeOverwritten() {
    String token = tokenUtils.generateToken(new SecurityTokenClaims(
        "user-1",
        "tenant-a",
        null,
        null,
        Set.of("order:read"),
        Map.of("sub", "other-user", "tenantCode", "other-tenant", "authorities", Set.of("admin"))));

    SecurityUser user = tokenUtils.parseUser(token);

    assertEquals("user-1", user.getUsername());
    assertEquals("tenant-a", user.tenantCode());
    assertEquals("order:read", user.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  void rejectWrongIssuer() {
    String token = tokenUtils.generateToken(
        new SecurityTokenClaims("user-1", "tenant-a", Set.of()));
    SecurityTokenUtils otherIssuer =
        new SecurityTokenUtils(properties("security-test-secret", "other-issuer"));

    assertFalse(otherIssuer.validateToken(token));
    assertTrue(tokenUtils.validateToken(token));
  }

  @Test
  void readPayloadsWhenOptionalTenantFieldsAreEmpty() {
    String token = tokenUtils.generateToken(
        new SecurityTokenClaims("user-1", "tenant-a", Set.of()));

    assertEquals("tenant-a", tokenUtils.getPayloads(token).get("tenantCode"));
  }

  private SecurityJwtProperties properties(String secret, String issuer) {
    SecurityJwtProperties properties = new SecurityJwtProperties();
    properties.setSecret(secret);
    properties.setIssuer(issuer);
    return properties;
  }
}
