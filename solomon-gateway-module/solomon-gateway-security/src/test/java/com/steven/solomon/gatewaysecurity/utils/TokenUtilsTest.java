package com.steven.solomon.gatewaysecurity.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.gatewaysecurity.code.AuthErrorCode;
import com.steven.solomon.gatewaysecurity.model.AccessTokenClaims;
import com.steven.solomon.gatewaysecurity.properties.AuthProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenUtilsTest {

  private TokenUtils tokenUtils;

  @BeforeEach
  void setUp() {
    AuthProperties properties = new AuthProperties();
    properties.setSecret("gateway-security-test-secret");
    tokenUtils = new TokenUtils(properties);
  }

  @Test
  void createAndParseToken() throws BaseException {
    String token = tokenUtils.createToken(
        new AccessTokenClaims("user-1", "tenant-1", List.of("order:query")));

    AccessTokenClaims claims = tokenUtils.parseToken(token);

    assertEquals("user-1", claims.userId());
    assertEquals("tenant-1", claims.tenantCode());
    assertEquals(List.of("order:query"), claims.permissions());
  }

  @Test
  void rejectInvalidToken() {
    BaseException exception =
        assertThrows(BaseException.class, () -> tokenUtils.parseToken("invalid-token"));

    assertEquals(AuthErrorCode.TOKEN_INVALID, exception.getCode());
  }
}
