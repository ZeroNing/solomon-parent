package com.steven.solomon.gateway.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtTokenUtilsTest {

  private static final String SECRET = "gateway-test-secret-32-bytes-minimum";

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
    JwtTokenUtils tokenUtils = tokenUtils(7200);
    String token = JWT.create()
        .setIssuer("gateway")
        .setSubject("user-1")
        .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
        .setExpiresAt(new Date(System.currentTimeMillis() - 1000))
        .setPayload("tenantCode", "tenant-1")
        .setSigner(JWTSignerUtil.hs256(SECRET.getBytes(CharsetUtil.CHARSET_UTF_8)))
        .sign();

    assertNull(tokenUtils.parseToken(token));
  }

  @Test
  void rejectUnsafeConfiguration() {
    GatewayJwtProperties properties = new GatewayJwtProperties();
    properties.setSecret("too-short");

    assertThrows(IllegalArgumentException.class, () -> new JwtTokenUtils(properties));
  }

  @Test
  void trimTrustedClaims() {
    JwtTokenUtils tokenUtils = tokenUtils(7200);

    GatewayTokenClaims claims =
        tokenUtils.parseToken(tokenUtils.createToken(new GatewayTokenClaims(" user-1 ", " tenant-1 ")));

    assertEquals("user-1", claims.userId());
    assertEquals("tenant-1", claims.tenantCode());
  }

  @Test
  void rejectUnsafeTenantCode() {
    JwtTokenUtils tokenUtils = tokenUtils(7200);

    assertThrows(IllegalArgumentException.class,
        () -> tokenUtils.createToken(new GatewayTokenClaims("user-1", "../tenant-1")));
  }

  @Test
  void useDefaultTenantInSingleMode() {
    JwtTokenUtils tokenUtils = tokenUtils(7200);

    GatewayTokenClaims claims =
        tokenUtils.parseToken(tokenUtils.createToken(new GatewayTokenClaims("user-1", null)));

    assertEquals("default", claims.tenantCode());
  }

  private JwtTokenUtils tokenUtils(long expireSeconds) {
    GatewayJwtProperties properties = new GatewayJwtProperties();
    properties.setSecret(SECRET);
    properties.setExpireSeconds(expireSeconds);
    return new JwtTokenUtils(properties);
  }
}
