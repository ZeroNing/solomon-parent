package com.steven.solomon.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.filter.TenantGatewayFilter;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class GatewayBearerAuthenticationConverterTest {

  private JwtTokenUtils tokenUtils;
  private GatewayBearerAuthenticationConverter converter;

  @BeforeEach
  void setUp() {
    GatewayJwtProperties properties = new GatewayJwtProperties();
    properties.setSecret("gateway-test-secret-32-bytes-minimum");
    tokenUtils = new JwtTokenUtils(properties);
    converter = new GatewayBearerAuthenticationConverter(tokenUtils);
  }

  @Test
  void convertValidTokenAndCacheClaims() {
    String token = tokenUtils.createToken(new GatewayTokenClaims("user-1", "tenant-1"));
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders")
        .header(GatewayHeaders.AUTHORIZATION, GatewayHeaders.BEARER_PREFIX + token));

    GatewayTokenClaims principal =
        assertInstanceOf(GatewayTokenClaims.class, converter.convert(exchange).block().getPrincipal());

    assertEquals("user-1", principal.userId());
    assertEquals("tenant-1", principal.tenantCode());
    assertEquals(principal, exchange.getAttribute(TenantGatewayFilter.CLAIMS_ATTRIBUTE));
  }

  @Test
  void ignoreRequestWithoutBearerToken() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login"));

    assertNull(converter.convert(exchange).block());
  }

  @Test
  void rejectInvalidToken() {
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders")
        .header(GatewayHeaders.AUTHORIZATION, GatewayHeaders.BEARER_PREFIX + "invalid"));

    assertThrows(IllegalArgumentException.class,
        () -> converter.convert(exchange).block());
  }
}
