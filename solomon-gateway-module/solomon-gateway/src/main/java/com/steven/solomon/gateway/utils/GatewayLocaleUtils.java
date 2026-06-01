package com.steven.solomon.gateway.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gateway.properties.GatewayI18nProperties;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * 网关国际化语言解析工具。
 */
public class GatewayLocaleUtils {

  private final GatewayI18nProperties properties;

  public GatewayLocaleUtils(GatewayI18nProperties properties) {
    this.properties = properties;
  }

  /**
   * 从 ServerRequest 中解析语言。
   */
  public Locale getLocale(ServerRequest request) {
    return getLocale(request.headers().firstHeader(HttpHeaders.ACCEPT_LANGUAGE));
  }

  /**
   * 从 ServerWebExchange 中解析语言。
   */
  public Locale getLocale(ServerWebExchange exchange) {
    return getLocale(exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT_LANGUAGE));
  }

  /**
   * 从 Accept-Language 请求头中解析受支持的语言。
   */
  public Locale getLocale(String acceptLanguage) {
    Locale defaultLocale = ObjectUtil.defaultIfNull(properties.getDefaultLocale(), Locale.CHINESE);
    List<Locale> supportedLocales = properties.getSupportedLocales();
    if (StrUtil.isBlank(acceptLanguage) || ObjectUtil.isEmpty(supportedLocales)) {
      return defaultLocale;
    }
    try {
      Locale matchedLocale = Locale.lookup(Locale.LanguageRange.parse(acceptLanguage), supportedLocales);
      return ObjectUtil.defaultIfNull(matchedLocale, defaultLocale);
    } catch (IllegalArgumentException exception) {
      return defaultLocale;
    }
  }
}
