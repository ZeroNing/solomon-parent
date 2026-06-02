package com.steven.solomon.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.gateway.code.GatewayErrorCode;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.handler.GatewayErrorWriter;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class TenantGatewayFilterTest {

  private JwtTokenUtils tokenUtils;

  @BeforeEach
  void setUp() {
    GatewayJwtProperties properties = new GatewayJwtProperties();
    properties.setSecret("gateway-test-secret");
    tokenUtils = new JwtTokenUtils(properties);
  }

  @Test
  void rejectRequestWithoutToken() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/orders"));

    filter(true).filter(exchange, current -> Mono.empty()).block();

    assertEquals(401, exchange.getResponse().getStatusCode().value());
    assertTrue(exchange.getResponse().getBodyAsString().block()
        .contains(GatewayErrorCode.TOKEN_REQUIRED));
  }

  @Test
  void writeTrustedTenantHeaders() {
    String token = tokenUtils.createToken(new GatewayTokenClaims("user-1", "tenant-1"));
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders")
        .header(GatewayHeaders.AUTHORIZATION, GatewayHeaders.BEARER_PREFIX + token)
        .header(GatewayHeaders.TENANT_CODE, "untrusted"));
    AtomicBoolean invoked = new AtomicBoolean();

    filter(true).filter(exchange, current -> {
      invoked.set(true);
      assertEquals("tenant-1",
          current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      assertEquals("user-1", current.getRequest().getHeaders().getFirst(GatewayHeaders.USER_ID));
      assertEquals("tenant-1", current.getAttribute(TenantGatewayFilter.TENANT_ATTRIBUTE));
      return Mono.empty();
    }).block();

    assertTrue(invoked.get());
  }

  @Test
  void rejectRequestByExternalValidator() {
    String token = tokenUtils.createToken(
        new GatewayTokenClaims("user-1", "tenant-1"));
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders")
        .header(GatewayHeaders.AUTHORIZATION, GatewayHeaders.BEARER_PREFIX + token));

    filter(false).filter(exchange, current -> Mono.empty()).block();

    assertEquals(401, exchange.getResponse().getStatusCode().value());
    assertTrue(exchange.getResponse().getBodyAsString().block()
        .contains(GatewayErrorCode.ACCESS_DENIED));
  }

  @Test
  void ignoredPathRemovesUntrustedHeaders() {
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/health")
        .header(GatewayHeaders.TENANT_CODE, "untrusted"));
    AtomicBoolean invoked = new AtomicBoolean();

    filter(true).filter(exchange, current -> {
      invoked.set(true);
      assertNull(current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      return Mono.empty();
    }).block();

    assertTrue(invoked.get());
  }

  @Test
  void publicTenantPathWritesValidatedTenantHeader() {
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login")
        .header(GatewayHeaders.TENANT_CODE, " tenant-1 "));
    AtomicBoolean invoked = new AtomicBoolean();

    filter(true).filter(exchange, current -> {
      invoked.set(true);
      assertEquals("tenant-1",
          current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      assertEquals("tenant-1", current.getAttribute(TenantGatewayFilter.TENANT_ATTRIBUTE));
      return Mono.empty();
    }).block();

    assertTrue(invoked.get());
  }

  private TenantGatewayFilter filter(boolean allowed) {
    return new TenantGatewayFilter(tokenUtils, new GatewayTenantProperties(),
        (tenantCode, exchange) -> Mono.just(allowed),
        (claims, exchange) -> Mono.just(allowed), new GatewayErrorWriter());
  }
}
