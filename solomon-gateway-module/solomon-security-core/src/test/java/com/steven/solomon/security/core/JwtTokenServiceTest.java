package com.steven.solomon.security.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

  @Test
  void shouldRequireExplicitSecret() {
    JwtTokenProperties properties = new JwtTokenProperties();

    assertThrows(IllegalArgumentException.class, () -> new JwtTokenService(properties));
  }

  @Test
  void shouldIssueTokenWithActiveKeyId() {
    JwtTokenProperties properties = new JwtTokenProperties();
    properties.setKeyId("key-2026");
    properties.setSecret("01234567890123456789012345678901");
    JwtTokenService service = new JwtTokenService(properties);

    String token = service.createToken(new TokenClaims("user-1", "tenant-1"));

    assertEquals("key-2026", JWT.of(token).getHeader("kid"));
    assertEquals(new TokenClaims("user-1", "tenant-1"), service.parseToken(token));
  }

  @Test
  void shouldVerifyTokenSignedByRotatedKey() {
    JwtTokenProperties oldProperties = new JwtTokenProperties();
    oldProperties.setKeyId("old");
    oldProperties.setSecrets(Map.of("old", "old-secret-0123456789012345678901"));
    JwtTokenService oldService = new JwtTokenService(oldProperties);
    String oldToken = oldService.createToken(new TokenClaims("user-1", "tenant-1"));

    JwtTokenProperties newProperties = new JwtTokenProperties();
    newProperties.setKeyId("new");
    newProperties.setSecrets(Map.of(
        "old", "old-secret-0123456789012345678901",
        "new", "new-secret-0123456789012345678901"));
    JwtTokenService newService = new JwtTokenService(newProperties);

    assertNotNull(newService.parseToken(oldToken));

    JwtTokenProperties missingOldKey = new JwtTokenProperties();
    missingOldKey.setKeyId("new");
    missingOldKey.setSecrets(Map.of("new", "new-secret-0123456789012345678901"));
    JwtTokenService missingOldKeyService = new JwtTokenService(missingOldKey);

    assertNull(missingOldKeyService.parseToken(oldToken));
  }

  @Test
  void shouldCreateRefreshTokenAndRejectItAsAccessToken() {
    JwtTokenService service = new JwtTokenService(properties());

    String refreshToken = service.createRefreshToken(new TokenClaims("user-1", "tenant-1"));

    assertNull(service.parseToken(refreshToken));
    assertEquals(new TokenClaims("user-1", "tenant-1"), service.parseRefreshToken(refreshToken));
  }

  @Test
  void shouldRotateRefreshTokenAndRevokeOldRefreshToken() {
    JwtTokenService service = new JwtTokenService(properties());
    TokenPair tokenPair = service.createTokenPair(new TokenClaims("user-1", "tenant-1"));

    TokenPair refreshed = service.refreshToken(tokenPair.refreshToken());

    assertNotNull(refreshed);
    assertEquals(new TokenClaims("user-1", "tenant-1"), service.parseToken(refreshed.accessToken()));
    assertNull(service.parseRefreshToken(tokenPair.refreshToken()));
  }

  @Test
  void shouldRejectRevokedAccessToken() {
    JwtTokenService service = new JwtTokenService(properties());
    String token = service.createToken(new TokenClaims("user-1", "tenant-1"));

    assertEquals(new TokenClaims("user-1", "tenant-1"), service.parseToken(token));
    assertEquals(true, service.revokeToken(token));

    assertNull(service.parseToken(token));
  }

  @Test
  void shouldAllowConfiguredClockSkewForRecentlyExpiredToken() {
    JwtTokenProperties properties = properties();
    properties.setClockSkewSeconds(120);
    JwtTokenService service = new JwtTokenService(properties);
    long now = System.currentTimeMillis();
    String token = JWT.create()
        .setIssuer(properties.getIssuer())
        .setSubject("user-1")
        .setIssuedAt(new Date(now - 120_000))
        .setExpiresAt(new Date(now - 30_000))
        .setPayload("tenantCode", "tenant-1")
        .setPayload("typ", "access")
        .setPayload("jti", "clock-skew-token")
        .setHeader("kid", properties.getKeyId())
        .sign(JWTSignerUtil.hs256(properties.getSecret().getBytes(CharsetUtil.CHARSET_UTF_8)));

    assertEquals(new TokenClaims("user-1", "tenant-1"), service.parseToken(token));
  }

  private JwtTokenProperties properties() {
    JwtTokenProperties properties = new JwtTokenProperties();
    properties.setKeyId("key-2026");
    properties.setSecret("01234567890123456789012345678901");
    properties.setRefreshExpireSeconds(3600);
    return properties;
  }
}
