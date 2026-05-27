package com.steven.solomon.cache.aspect;

import com.steven.solomon.cache.util.CacheDigestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 重复请求指纹生成器。
 */
public class RequestFingerprintBuilder {

  private static final String AUTHORIZATION = "Authorization";

  private static final String TOKEN = "token";

  private static final String ACCESS_TOKEN = "access_token";

  public String build() {
    return build("none-web-request");
  }

  public String build(String fallback) {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      return CacheDigestUtils.sha256(fallback);
    }
    HttpServletRequest request = attributes.getRequest();
    return CacheDigestUtils.sha256(
        request.getMethod() + ":" + requestUrl(request) + ":" + token(request));
  }

  private String requestUrl(HttpServletRequest request) {
    String queryString = request.getQueryString();
    if (queryString == null || queryString.isBlank()) {
      return request.getRequestURL().toString();
    }
    return request.getRequestURL() + "?" + queryString;
  }

  private String token(HttpServletRequest request) {
    String value = request.getHeader(AUTHORIZATION);
    if (hasText(value)) {
      return value;
    }
    value = request.getHeader(TOKEN);
    if (hasText(value)) {
      return value;
    }
    value = request.getHeader(ACCESS_TOKEN);
    return hasText(value) ? value : "";
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

}
