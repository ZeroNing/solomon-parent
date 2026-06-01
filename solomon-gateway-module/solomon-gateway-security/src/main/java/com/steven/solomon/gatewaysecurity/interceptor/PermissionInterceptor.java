package com.steven.solomon.gatewaysecurity.interceptor;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.gatewaysecurity.annotation.ApiPermission;
import com.steven.solomon.gatewaysecurity.code.AuthErrorCode;
import com.steven.solomon.gatewaysecurity.constant.AuthHeaders;
import com.steven.solomon.gatewaysecurity.context.AuthContext;
import com.steven.solomon.gatewaysecurity.model.AccessTokenClaims;
import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;
import com.steven.solomon.gatewaysecurity.service.PermissionVerifier;
import com.steven.solomon.gatewaysecurity.service.TenantVerifier;
import com.steven.solomon.gatewaysecurity.utils.PermissionCodeUtils;
import com.steven.solomon.gatewaysecurity.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 根据接口注解执行多租户 Token 鉴权。
 *
 * <p>无注解接口允许匿名访问；有注解接口根据 anonymous 属性决定是否强制校验 Token。</p>
 */
public class PermissionInterceptor implements HandlerInterceptor {

  public static final String AUTH_CLAIMS_ATTRIBUTE = PermissionInterceptor.class.getName() + ".claims";

  private final TokenUtils tokenUtils;
  private final TenantVerifier tenantVerifier;
  private final PermissionVerifier permissionVerifier;

  public PermissionInterceptor(TokenUtils tokenUtils, TenantVerifier tenantVerifier,
      PermissionVerifier permissionVerifier) {
    this.tokenUtils = tokenUtils;
    this.tenantVerifier = tenantVerifier;
    this.permissionVerifier = permissionVerifier;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    AuthContext.clear();
    String token = tokenUtils.resolveBearerToken(request.getHeader(AuthHeaders.AUTHORIZATION));
    ApiPermission annotation = resolveAnnotation(handler);
    if (annotation == null) {
      // 无注解 → 完全匿名，尝试解析但不强制
      AccessTokenClaims claims = tokenUtils.parseOptionalToken(token);
      if (claims != null) {
        validateTenant(request, claims);
        AuthContext.set(claims);
        request.setAttribute(AUTH_CLAIMS_ATTRIBUTE, claims);
      }
      return true;
    }

    boolean anonymous = annotation.anonymous();
    AccessTokenClaims claims = anonymous
        ? tokenUtils.parseOptionalToken(token)
        : tokenUtils.parseToken(token);
    if (claims == null) {
      return true;
    }
    validateTenant(request, claims);
    if (!anonymous && !permissionVerifier.hasPermission(
        claims, toPermission(request, annotation, handler))) {
      throw new BaseException(AuthErrorCode.PERMISSION_DENIED);
    }
    AuthContext.set(claims);
    request.setAttribute(AUTH_CLAIMS_ATTRIBUTE, claims);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    AuthContext.clear();
  }

  private void validateTenant(HttpServletRequest request, AccessTokenClaims claims)
      throws BaseException {
    if (StrUtil.isBlank(claims.tenantCode())) {
      throw new BaseException(AuthErrorCode.TENANT_REQUIRED);
    }
    String headerTenantCode = request.getHeader(AuthHeaders.TENANT_CODE);
    if (StrUtil.isNotBlank(headerTenantCode)
        && !StrUtil.equals(headerTenantCode, claims.tenantCode())) {
      throw new BaseException(AuthErrorCode.TENANT_FORBIDDEN);
    }
    if (!tenantVerifier.isAvailable(claims)) {
      throw new BaseException(AuthErrorCode.TENANT_FORBIDDEN);
    }
  }

  private ApiPermission resolveAnnotation(Object handler) {
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return null;
    }
    ApiPermission annotation =
        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), ApiPermission.class);
    return annotation != null ? annotation
        : AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), ApiPermission.class);
  }

  private PermissionDefinition toPermission(HttpServletRequest request, ApiPermission annotation,
      Object handler) {
    String code = StrUtil.blankToDefault(annotation.value(),
        PermissionCodeUtils.pathToCode(request.getRequestURI()));
    String name = resolveName(annotation, handler);
    return new PermissionDefinition(code, name, annotation.description(),
        request.getRequestURI(), request.getMethod(), annotation.anonymous());
  }

  private String resolveName(ApiPermission annotation, Object handler) {
    if (StrUtil.isNotBlank(annotation.name())) {
      return annotation.name();
    }
    if (handler instanceof HandlerMethod handlerMethod) {
      String methodName = PermissionCodeUtils.resolveOperationName(handlerMethod.getMethod());
      if (StrUtil.isNotBlank(methodName)) {
        return methodName;
      }
      return PermissionCodeUtils.resolveOperationName(handlerMethod.getBeanType());
    }
    return "";
  }
}
