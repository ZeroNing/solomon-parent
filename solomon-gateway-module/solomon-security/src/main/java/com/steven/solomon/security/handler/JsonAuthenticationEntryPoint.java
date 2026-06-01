package com.steven.solomon.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 未登录或 Token 无效响应处理器。
 */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    SecurityErrorWriter.write(
        response,
        HttpStatus.UNAUTHORIZED.value(),
        "SECURITY_UNAUTHORIZED",
        exception.getMessage());
  }
}
