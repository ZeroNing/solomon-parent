package com.steven.solomon.gateway.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.gateway.handler.GatewayErrorWriter;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.utils.JwtTokenUtils;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.config.web.server.ServerHttpSecurity;

class GatewayAutoConfigurationTest {

  private final GatewayAutoConfiguration configuration = new GatewayAutoConfiguration();

  @Test
  void denyAccessWhenBusinessValidatorIsMissing() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/orders"));

    assertFalse(configuration.gatewayAccessValidator()
        .validate(new GatewayTokenClaims("user-1", "tenant-1"), exchange).block());
  }

  @Test
  void denyTenantWhenBusinessValidatorIsMissing() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/auth/login"));

    assertFalse(configuration.gatewayTenantValidator().validate("tenant-1", exchange).block());
  }

  @Test
  void buildSecurityChainWhenIgnoredPathsAreEmpty() {
    GatewayJwtProperties jwtProperties = new GatewayJwtProperties();
    jwtProperties.setSecret("gateway-test-secret-32-bytes-minimum");
    GatewayTenantProperties tenantProperties = new GatewayTenantProperties();
    tenantProperties.setIgnoredPaths(Collections.emptyList());
    tenantProperties.setPublicTenantPaths(Collections.emptyList());

    assertNotNull(new GatewaySecurityAutoConfiguration().gatewaySecurityWebFilterChain(
        ServerHttpSecurity.http(), new JwtTokenUtils(jwtProperties), tenantProperties,
        new GatewayErrorWriter(), List::of));
  }

  @Test
  void rejectPublicTenantPathOutsideIgnoredPaths() {
    GatewayTenantProperties tenantProperties = new GatewayTenantProperties();
    tenantProperties.setIgnoredPaths(List.of("/actuator/health"));
    tenantProperties.setPublicTenantPaths(List.of("/auth/**"));

    assertThrows(IllegalArgumentException.class, tenantProperties::validate);
  }
}
