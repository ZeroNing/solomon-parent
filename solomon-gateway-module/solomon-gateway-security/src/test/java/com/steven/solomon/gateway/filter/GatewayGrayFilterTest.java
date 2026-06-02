package com.steven.solomon.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.gateway.gray.GatewayGraySelector;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayGrayProperties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class GatewayGrayFilterTest {

  @Test
  void removeUntrustedGrayHeaderWhenDisabled() {
    GatewayGrayProperties properties = new GatewayGrayProperties();
    GatewayGrayFilter filter = filter(properties);
    MockServerWebExchange exchange = MockServerWebExchange.from(
        MockServerHttpRequest.get("/orders").header(properties.getHeaderName(), "forged"));
    AtomicReference<String> header = new AtomicReference<>();

    filter.filter(exchange, current -> {
      header.set(current.getRequest().getHeaders().getFirst(properties.getHeaderName()));
      return Mono.empty();
    }).block();

    assertNull(header.get());
  }

  @Test
  void overwriteUntrustedGrayHeaderWithTrustedVersion() {
    GatewayGrayProperties properties = new GatewayGrayProperties();
    properties.setEnabled(true);
    properties.setCandidateWeight(100);
    GatewayGrayFilter filter = filter(properties);
    MockServerWebExchange exchange = MockServerWebExchange.from(
        MockServerHttpRequest.get("/orders").header(properties.getHeaderName(), "forged"));
    exchange.getAttributes().put(TenantGatewayFilter.CLAIMS_ATTRIBUTE,
        new GatewayTokenClaims("user-1", "tenant-1"));

    filter.filter(exchange, current -> {
      assertEquals("gray",
          current.getRequest().getHeaders().getFirst(properties.getHeaderName()));
      assertEquals("gray", current.getAttribute(GatewayGrayFilter.VERSION_ATTRIBUTE));
      return Mono.empty();
    }).block();
  }

  @Test
  void rejectInvalidWeight() {
    GatewayGrayProperties properties = new GatewayGrayProperties();
    properties.setCandidateWeight(101);

    assertThrows(IllegalArgumentException.class, () -> new GatewayGraySelector(properties));
  }

  private GatewayGrayFilter filter(GatewayGrayProperties properties) {
    return new GatewayGrayFilter(properties, new GatewayGraySelector(properties));
  }
}
