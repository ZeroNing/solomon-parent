package com.steven.solomon.gatewaysecurity.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.gatewaysecurity.annotation.ApiPermission;
import com.steven.solomon.gatewaysecurity.code.AuthErrorCode;
import com.steven.solomon.gatewaysecurity.constant.AuthHeaders;
import com.steven.solomon.gatewaysecurity.context.AuthContext;
import com.steven.solomon.gatewaysecurity.model.AccessTokenClaims;
import com.steven.solomon.gatewaysecurity.properties.AuthProperties;
import com.steven.solomon.gatewaysecurity.utils.TokenUtils;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

class PermissionInterceptorTest {

  private TokenUtils tokenUtils;
  private PermissionInterceptor interceptor;

  @BeforeEach
  void setUp() {
    AuthProperties properties = new AuthProperties();
    properties.setSecret("gateway-security-test-secret");
    tokenUtils = new TokenUtils(properties);
    interceptor = new PermissionInterceptor(tokenUtils, claims -> true,
        (claims, permission) -> claims.permissions().contains(permission.code()));
  }

  @AfterEach
  void tearDown() {
    AuthContext.clear();
  }

  @Test
  void allowAnonymousPublicEndpoint() throws Exception {
    assertTrue(interceptor.preHandle(request("/public", null), response(), handler("publicApi")));
    assertNull(AuthContext.get());
  }

  @Test
  void allowPublicEndpointWithInvalidToken() throws Exception {
    assertTrue(interceptor.preHandle(request("/public", "invalid-token"), response(),
        handler("publicApi")));
    assertNull(AuthContext.get());
  }

  @Test
  void rejectProtectedEndpointWithoutToken() throws Exception {
    BaseException exception = catchBaseException(
        () -> interceptor.preHandle(request("/orders", null), response(), handler("orders")));

    assertEquals(AuthErrorCode.TOKEN_REQUIRED, exception.getCode());
  }

  @Test
  void rejectPermissionDenied() throws Exception {
    String token = tokenUtils.createToken(new AccessTokenClaims("user-1", "tenant-1", List.of()));
    BaseException exception = catchBaseException(() ->
        interceptor.preHandle(request("/orders", token), response(), handler("orders")));

    assertEquals(AuthErrorCode.PERMISSION_DENIED, exception.getCode());
  }

  @Test
  void rejectTenantHeaderMismatch() throws Exception {
    String token =
        tokenUtils.createToken(new AccessTokenClaims("user-1", "tenant-1", List.of("order:query")));
    MockHttpServletRequest request = request("/orders", token);
    request.addHeader(AuthHeaders.TENANT_CODE, "tenant-2");

    BaseException exception = catchBaseException(() ->
        interceptor.preHandle(request, response(), handler("orders")));

    assertEquals(AuthErrorCode.TENANT_FORBIDDEN, exception.getCode());
  }

  @Test
  void allowProtectedEndpointWithPermission() throws Exception {
    String token =
        tokenUtils.createToken(new AccessTokenClaims("user-1", "tenant-1", List.of("order:query")));

    assertTrue(interceptor.preHandle(request("/orders", token), response(), handler("orders")));
    assertEquals("tenant-1", AuthContext.getTenantCode());
  }

  @Test
  void allowAnonymousAnnotatedEndpoint() throws Exception {
    assertTrue(interceptor.preHandle(request("/public", null), response(), handler("anonymousApi")));
    assertNull(AuthContext.get());
  }

  @Test
  void allowAnonymousAnnotatedEndpointWithInvalidToken() throws Exception {
    assertTrue(interceptor.preHandle(request("/public", "invalid-token"), response(),
        handler("anonymousApi")));
    assertNull(AuthContext.get());
  }

  private MockHttpServletRequest request(String path, String token) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    if (token != null) {
      request.addHeader(AuthHeaders.AUTHORIZATION, AuthHeaders.BEARER_PREFIX + token);
    }
    return request;
  }

  private MockHttpServletResponse response() {
    return new MockHttpServletResponse();
  }

  private HandlerMethod handler(String methodName) throws NoSuchMethodException {
    Method method = TestController.class.getDeclaredMethod(methodName);
    return new HandlerMethod(new TestController(), method);
  }

  private BaseException catchBaseException(CheckedRunnable runnable) throws Exception {
    try {
      runnable.run();
      throw new AssertionError("预期抛出 BaseException");
    } catch (BaseException ex) {
      return ex;
    }
  }

  @FunctionalInterface
  private interface CheckedRunnable {

    void run() throws Exception;
  }

  private static class TestController {

    public void publicApi() {
    }

    @ApiPermission(value = "order:query", name = "查询订单")
    public void orders() {
    }

    @ApiPermission(anonymous = true)
    public void anonymousApi() {
    }
  }
}
