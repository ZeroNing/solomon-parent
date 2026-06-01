package com.steven.solomon.gateway.handler;

import cn.hutool.json.JSONUtil;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关租户异常响应工具。
 */
public final class GatewayTenantErrorWriter {

  private GatewayTenantErrorWriter() {
  }

  /**
   * 写出租户校验失败响应。
   */
  public static Mono<Void> write(ServerWebExchange exchange, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", HttpStatus.UNAUTHORIZED.value());
    body.put("errorCode", "GATEWAY_TENANT_UNAUTHORIZED");
    body.put("message", message);
    byte[] bytes = JSONUtil.toJsonStr(body).getBytes(StandardCharsets.UTF_8);
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }
}
