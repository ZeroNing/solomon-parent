package com.steven.solomon.security.handler;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;

/**
 * 安全异常 JSON 响应工具。
 */
public final class SecurityErrorWriter {

  private SecurityErrorWriter() {
  }

  /**
   * 写出统一错误响应。
   */
  public static void write(HttpServletResponse response, int status, String code, String message)
      throws IOException {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status);
    body.put("errorCode", code);
    body.put("message", message);
    response.setStatus(status);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(JSONUtil.toJsonStr(body));
  }
}
