package com.steven.solomon.gateway.handler;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.gateway.code.GatewayErrorCode;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 网关国际化错误响应输出器。 */
public class GatewayErrorWriter {

  /** 返回统一 JSON 错误响应。 */
  public Mono<Void> write(ServerWebExchange exchange, String code) {
    Locale locale = resolveLocale(exchange);
    String message = I18nUtils.getErrorMessage(code, locale);
    HttpStatus status = resolveStatus(code);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status.value());
    body.put("errorCode", code);
    body.put("message", StrUtil.blankToDefault(message, code));
    byte[] bytes = JSONUtil.toJsonStr(body).getBytes(CharsetUtil.CHARSET_UTF_8);
    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }

  private HttpStatus resolveStatus(String code) {
    return GatewayErrorCode.ACCESS_DENIED.equals(code) || GatewayErrorCode.TENANT_INVALID.equals(code)
        ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
  }

  private Locale resolveLocale(ServerWebExchange exchange) {
    Locale locale = exchange.getLocaleContext().getLocale();
    return locale == null ? LocaleContextHolder.getLocale() : locale;
  }
}
