package com.steven.solomon.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.security.constant.SecurityHeaders;
import com.steven.solomon.security.handler.JsonAuthenticationEntryPoint;
import com.steven.solomon.security.model.SecurityTokenClaims;
import com.steven.solomon.security.properties.SecurityJwtProperties;
import com.steven.solomon.security.properties.SecurityTenantProperties;
import com.steven.solomon.security.service.TenantAccessValidator;
import com.steven.solomon.security.service.TokenAccessValidator;
import com.steven.solomon.security.utils.SecurityContextUtils;
import com.steven.solomon.security.utils.SecurityTokenUtils;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class TenantAuthenticationFilterTest {

  private SecurityTokenUtils tokenUtils;
  private SecurityTenantProperties tenantProperties;

  @BeforeEach
  void setUp() {
    SecurityJwtProperties jwtProperties = new SecurityJwtProperties();
    jwtProperties.setSecret("security-test-secret");
    tokenUtils = new SecurityTokenUtils(jwtProperties);
    tenantProperties = new SecurityTenantProperties();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    RequestHeaderHolder.remove();
  }

  @Test
  void writeAndClearTenantSecurityContext() throws Exception {
    TenantAuthenticationFilter filter = filter(user -> true);
    MockHttpServletRequest request = request("tenant-a");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean invoked = new AtomicBoolean();

    filter.doFilter(request, response, (servletRequest, servletResponse) -> {
      invoked.set(true);
      assertEquals("tenant-a", SecurityContextUtils.getTenantCode());
      assertEquals("tenant-a", RequestHeaderHolder.getTenantCode());
      assertTrue(SecurityContextUtils.getCurrentUser().getAuthorities().stream()
          .anyMatch(authority -> authority.getAuthority().equals("order:read")));
    });

    assertTrue(invoked.get());
    assertNull(SecurityContextUtils.getCurrentUser());
    assertEquals("", RequestHeaderHolder.getTenantCode());
  }

  @Test
  void rejectTenantHeaderMismatch() throws Exception {
    TenantAuthenticationFilter filter = filter(user -> true);
    MockHttpServletRequest request = request("tenant-a");
    request.addHeader(SecurityHeaders.TENANT_CODE, "tenant-b");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean invoked = new AtomicBoolean();

    filter.doFilter(request, response, (servletRequest, servletResponse) -> invoked.set(true));

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("SECURITY_UNAUTHORIZED"));
    assertEquals(false, invoked.get());
  }

  @Test
  void rejectTenantByBusinessValidator() throws Exception {
    TenantAuthenticationFilter filter = filter(user -> false);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request("tenant-a"), response, (servletRequest, servletResponse) -> {
    });

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("当前用户无权访问该租户"));
  }

  @Test
  void rejectRevokedToken() throws Exception {
    TenantAuthenticationFilter filter = filter(user -> true, (token, user) -> false);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request("tenant-a"), response, (servletRequest, servletResponse) -> {
    });

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("Token 已失效"));
  }

  private TenantAuthenticationFilter filter(TenantAccessValidator validator) {
    return filter(validator, (token, user) -> true);
  }

  private TenantAuthenticationFilter filter(
      TenantAccessValidator validator,
      TokenAccessValidator tokenValidator) {
    return new TenantAuthenticationFilter(
        tokenUtils,
        tenantProperties,
        validator,
        tokenValidator,
        new JsonAuthenticationEntryPoint());
  }

  private MockHttpServletRequest request(String tenantCode) {
    String token = tokenUtils.generateToken(
        new SecurityTokenClaims("user-1", tenantCode, Set.of("order:read")));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(SecurityHeaders.AUTHORIZATION, "Bearer " + token);
    return request;
  }
}
