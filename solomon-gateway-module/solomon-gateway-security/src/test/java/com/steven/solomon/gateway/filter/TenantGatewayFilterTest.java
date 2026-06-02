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
import com.steven.solomon.gateway.properties.GatewaySecurityMode;
import com.steven.solomon.gateway.properties.GatewaySecurityProperties;
import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import java.util.Collections;
import java.util.List;
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
    properties.setSecret("gateway-test-secret-32-bytes-minimum");
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
  void reuseClaimsValidatedBySecurityFilter() {
    GatewayTokenClaims claims = new GatewayTokenClaims("user-1", "tenant-1");
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders")
        .header(GatewayHeaders.AUTHORIZATION, GatewayHeaders.BEARER_PREFIX + "already-validated"));
    exchange.getAttributes().put(TenantGatewayFilter.CLAIMS_ATTRIBUTE, claims);
    AtomicBoolean invoked = new AtomicBoolean();

    filter(true).filter(exchange, current -> {
      invoked.set(true);
      assertEquals("tenant-1",
          current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      assertEquals("user-1", current.getRequest().getHeaders().getFirst(GatewayHeaders.USER_ID));
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

    assertEquals(403, exchange.getResponse().getStatusCode().value());
    assertTrue(exchange.getResponse().getBodyAsString().block()
        .contains(GatewayErrorCode.ACCESS_DENIED));
  }

  @Test
  void ignoredPathRemovesUntrustedHeaders() {
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/health")
        .header(GatewayHeaders.TENANT_CODE, "untrusted")
        .header(GatewayHeaders.TENANT_ID, "untrusted-id")
        .header(GatewayHeaders.TENANT_NAME, "untrusted-name"));
    AtomicBoolean invoked = new AtomicBoolean();

    filter(true).filter(exchange, current -> {
      invoked.set(true);
      assertNull(current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      assertNull(current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_ID));
      assertNull(current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_NAME));
      return Mono.empty();
    }).block();

    assertTrue(invoked.get());
  }

  @Test
  void annotationAnonymousPathDoesNotRequireToken() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/public/ping"));
    AtomicBoolean invoked = new AtomicBoolean();
    TenantGatewayFilter filter = new TenantGatewayFilter(tokenUtils, new GatewayTenantProperties(),
        (tenantCode, current) -> Mono.just(true), (claims, current) -> Mono.just(true),
        new GatewayErrorWriter(), new TenantModeResolver(new TenantModeProperties()),
        new GatewaySecurityProperties(), () -> List.of("/api/public/**"));

    filter.filter(exchange, current -> {
      invoked.set(true);
      return Mono.empty();
    }).block();

    assertTrue(invoked.get());
  }

  @Test
  void rejectUnsafePublicTenantHeader() {
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login")
        .header(GatewayHeaders.TENANT_CODE, String.join("", Collections.nCopies(129, "a"))));

    filter(true).filter(exchange, current -> Mono.empty()).block();

    assertEquals(403, exchange.getResponse().getStatusCode().value());
    assertTrue(exchange.getResponse().getBodyAsString().block()
        .contains(GatewayErrorCode.TENANT_INVALID));
  }

  @Test
  void rejectPublicTenantHeaderWithUnsafeCharacters() {
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login")
        .header(GatewayHeaders.TENANT_CODE, "../tenant-1"));

    filter(true).filter(exchange, current -> Mono.empty()).block();

    assertEquals(403, exchange.getResponse().getStatusCode().value());
    assertTrue(exchange.getResponse().getBodyAsString().block()
        .contains(GatewayErrorCode.TENANT_INVALID));
  }

  @Test
  void rejectWhenExternalValidatorFails() {
    String token = tokenUtils.createToken(new GatewayTokenClaims("user-1", "tenant-1"));
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders")
        .header(GatewayHeaders.AUTHORIZATION, GatewayHeaders.BEARER_PREFIX + token));
    TenantGatewayFilter filter = new TenantGatewayFilter(tokenUtils, new GatewayTenantProperties(),
        (tenantCode, current) -> {
          throw new IllegalStateException("validator unavailable");
        },
        (claims, current) -> Mono.just(true), new GatewayErrorWriter());

    filter.filter(exchange, current -> Mono.empty()).block();

    assertEquals(403, exchange.getResponse().getStatusCode().value());
    assertTrue(exchange.getResponse().getBodyAsString().block()
        .contains(GatewayErrorCode.ACCESS_DENIED));
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

  @Test
  void publicTenantPathUsesDefaultTenantInSingleMode() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login"));
    AtomicBoolean invoked = new AtomicBoolean();

    filter(true).filter(exchange, current -> {
      invoked.set(true);
      assertEquals("default",
          current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      return Mono.empty();
    }).block();

    assertTrue(invoked.get());
  }

  @Test
  void standaloneModeKeepsIdentityOnlyInExchangeAttributes() {
    String token = tokenUtils.createToken(new GatewayTokenClaims("user-1", "tenant-1"));
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders")
        .header(GatewayHeaders.AUTHORIZATION, GatewayHeaders.BEARER_PREFIX + token)
        .header(GatewayHeaders.TENANT_CODE, "untrusted"));
    GatewaySecurityProperties securityProperties = new GatewaySecurityProperties();
    securityProperties.setMode(GatewaySecurityMode.STANDALONE);
    TenantGatewayFilter filter = new TenantGatewayFilter(tokenUtils, new GatewayTenantProperties(),
        (tenantCode, current) -> Mono.just(true), (claims, current) -> Mono.just(true),
        new GatewayErrorWriter(), new TenantModeResolver(new TenantModeProperties()),
        securityProperties, List::of);

    filter.filter(exchange, current -> {
      assertNull(current.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE));
      assertNull(current.getRequest().getHeaders().getFirst(GatewayHeaders.USER_ID));
      assertEquals("tenant-1", current.getAttribute(TenantGatewayFilter.TENANT_ATTRIBUTE));
      return Mono.empty();
    }).block();
  }

  private TenantGatewayFilter filter(boolean allowed) {
    return new TenantGatewayFilter(tokenUtils, new GatewayTenantProperties(),
        (tenantCode, exchange) -> Mono.just(allowed),
        (claims, exchange) -> Mono.just(allowed), new GatewayErrorWriter());
  }
}
