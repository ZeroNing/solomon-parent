package com.steven.solomon.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.utils.GatewayTenantUtils;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class GatewayTenantFilterTest {

  private GatewayTenantFilter filter;

  @BeforeEach
  void setUp() {
    GatewayJwtProperties jwtProperties = new GatewayJwtProperties();
    jwtProperties.setSecret("gateway-sdk-test-secret");
    GatewayTenantUtils tenantUtils = new GatewayTenantUtils(
        new JwtTokenUtils(jwtProperties),
        new GatewayTenantProperties(),
        (tenantCode, exchange) -> true);
    filter = new GatewayTenantFilter(tenantUtils);
  }

  @Test
  void rejectRequestWithoutTenant() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/orders"));

    filter.filter(exchange, currentExchange -> Mono.empty()).block();

    assertEquals(401, exchange.getResponse().getStatusCode().value());
    String body = exchange.getResponse().getBodyAsString().block();
    assertTrue(body.contains("GATEWAY_TENANT_UNAUTHORIZED"));
  }

  @Test
  void ignoredPathRemovesUntrustedTenantHeader() {
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login")
        .header(GatewayHeaders.TENANT_CODE, "untrusted-tenant"));
    AtomicBoolean invoked = new AtomicBoolean();

    filter.filter(exchange, currentExchange -> {
      invoked.set(true);
      assertNull(currentExchange.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      return Mono.empty();
    }).block();

    assertTrue(invoked.get());
  }
}
