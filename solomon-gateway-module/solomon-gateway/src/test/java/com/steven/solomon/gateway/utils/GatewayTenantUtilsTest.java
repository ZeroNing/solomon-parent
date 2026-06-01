package com.steven.solomon.gateway.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

class GatewayTenantUtilsTest {

  private JwtTokenUtils tokenUtils;
  private GatewayTenantProperties tenantProperties;
  private GatewayTenantUtils tenantUtils;

  @BeforeEach
  void setUp() {
    GatewayJwtProperties jwtProperties = new GatewayJwtProperties();
    jwtProperties.setSecret("gateway-sdk-test-secret");
    tokenUtils = new JwtTokenUtils(jwtProperties);
    tenantProperties = new GatewayTenantProperties();
    tenantUtils = new GatewayTenantUtils(tokenUtils, tenantProperties, (tenantCode, exchange) -> true);
  }

  @Test
  void resolveTokenTenantCode() {
    String token = tokenUtils.generateToken(new GatewayTokenClaims("user-1", "tenant-a"));
    ServerWebExchange exchange = exchange("/")
        .mutate()
        .request(builder -> builder.header(GatewayHeaders.AUTHORIZATION, "Bearer " + token))
        .build();

    assertEquals("tenant-a", tenantUtils.resolveTenantCode(exchange));
  }

  @Test
  void rejectTenantHeaderMismatch() {
    String token = tokenUtils.generateToken(new GatewayTokenClaims("user-1", "tenant-token"));
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
        .header(GatewayHeaders.AUTHORIZATION, "Bearer " + token)
        .header(GatewayHeaders.TENANT_CODE, "tenant-header"));

    assertThrows(IllegalArgumentException.class, () -> tenantUtils.resolveTenantCode(exchange));
  }

  @Test
  void rejectInvalidToken() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
        .header(GatewayHeaders.AUTHORIZATION, "Bearer invalid-token")
        .header(GatewayHeaders.TENANT_CODE, "tenant-header"));

    assertThrows(IllegalArgumentException.class, () -> tenantUtils.resolveTenantCode(exchange));
  }

  @Test
  void rejectMissingTenant() {
    assertThrows(IllegalArgumentException.class, () -> tenantUtils.resolveTenantCode(exchange("/")));
  }

  @Test
  void cleanAndWriteTrustedTenantHeader() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
        .header(GatewayHeaders.TENANT_CODE, "untrusted-tenant"));

    ServerWebExchange mutatedExchange = tenantUtils.writeTenantCode(exchange, "trusted-tenant");

    assertEquals("trusted-tenant",
        mutatedExchange.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
  }

  @Test
  void matchIgnoredPath() {
    assertTrue(tenantUtils.isIgnoredPath(exchange("/auth/login")));
  }

  @Test
  void rejectTenantByBusinessValidator() {
    GatewayTenantUtils rejectingUtils =
        new GatewayTenantUtils(tokenUtils, tenantProperties, (tenantCode, exchange) -> false);
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
        .header(GatewayHeaders.TENANT_CODE, "tenant-a"));

    assertThrows(IllegalArgumentException.class, () -> rejectingUtils.resolveTenantCode(exchange));
  }

  private ServerWebExchange exchange(String path) {
    return MockServerWebExchange.from(MockServerHttpRequest.get(path));
  }
}
