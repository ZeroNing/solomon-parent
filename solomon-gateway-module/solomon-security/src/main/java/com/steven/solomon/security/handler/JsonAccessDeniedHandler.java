package com.steven.solomon.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 权限不足响应处理器。
 */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException exception) throws IOException, ServletException {
    SecurityErrorWriter.write(
        response,
        HttpStatus.FORBIDDEN.value(),
        "SECURITY_FORBIDDEN",
        exception.getMessage());
  }
}
