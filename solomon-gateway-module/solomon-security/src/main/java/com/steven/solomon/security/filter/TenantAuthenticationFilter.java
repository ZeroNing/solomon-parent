package com.steven.solomon.security.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.security.constant.SecurityHeaders;
import com.steven.solomon.security.handler.JsonAuthenticationEntryPoint;
import com.steven.solomon.security.model.SecurityUser;
import com.steven.solomon.security.properties.SecurityTenantProperties;
import com.steven.solomon.security.service.TenantAccessValidator;
import com.steven.solomon.security.service.TokenAccessValidator;
import com.steven.solomon.security.utils.SecurityTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 多租户 JWT 鉴权过滤器。
 */
public class TenantAuthenticationFilter extends OncePerRequestFilter {

  private final SecurityTokenUtils tokenUtils;
  private final SecurityTenantProperties tenantProperties;
  private final TenantAccessValidator tenantAccessValidator;
  private final TokenAccessValidator tokenAccessValidator;
  private final JsonAuthenticationEntryPoint authenticationEntryPoint;

  public TenantAuthenticationFilter(
      SecurityTokenUtils tokenUtils,
      SecurityTenantProperties tenantProperties,
      TenantAccessValidator tenantAccessValidator,
      TokenAccessValidator tokenAccessValidator,
      JsonAuthenticationEntryPoint authenticationEntryPoint) {
    this.tokenUtils = tokenUtils;
    this.tenantProperties = tenantProperties;
    this.tenantAccessValidator = tenantAccessValidator;
    this.tokenAccessValidator = tokenAccessValidator;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    try {
      String token = request.getHeader(SecurityHeaders.AUTHORIZATION);
      if (StrUtil.isBlank(token)) {
        chain.doFilter(request, response);
        return;
      }
      SecurityUser user = tokenUtils.parseUser(token);
      validateTenant(request, user);
      if (!tokenAccessValidator.validate(token, user)) {
        throw new IllegalArgumentException("Token 已失效");
      }
      writeContext(user);
      chain.doFilter(request, response);
    } catch (IllegalArgumentException exception) {
      SecurityContextHolder.clearContext();
      RequestHeaderHolder.remove();
      authenticationEntryPoint.commence(
          request,
          response,
          new BadCredentialsException(exception.getMessage(), exception));
    } finally {
      SecurityContextHolder.clearContext();
      RequestHeaderHolder.remove();
    }
  }

  private void validateTenant(HttpServletRequest request, SecurityUser user) {
    if (tenantProperties.isRequired() && StrUtil.isBlank(user.tenantCode())) {
      throw new IllegalArgumentException("Token 缺少租户编码");
    }
    String headerTenantCode = request.getHeader(tenantProperties.getHeaderName());
    if (tenantProperties.isValidateHeader()
        && StrUtil.isNotBlank(headerTenantCode)
        && !StrUtil.equals(headerTenantCode, user.tenantCode())) {
      throw new IllegalArgumentException("请求租户与 Token 租户不一致");
    }
    if (ObjectUtil.isNotEmpty(tenantAccessValidator) && !tenantAccessValidator.validate(user)) {
      throw new IllegalArgumentException("当前用户无权访问该租户");
    }
  }

  private void writeContext(SecurityUser user) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    RequestHeaderHolder.setTenantCode(user.tenantCode());
    RequestHeaderHolder.setTenantId(user.tenantId());
    RequestHeaderHolder.setTenantName(user.tenantName());
  }
}
