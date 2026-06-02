package com.steven.solomon.cache.aspect;

import com.steven.solomon.cache.util.CacheDigestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求指纹生成器。
 *
 * <p>基于当前 HTTP 请求的方法、URL（含查询参数）和用户 token
 * 生成唯一指纹（SHA-256），用于 {@link RepeatRequestLimitAspect} 判定重复请求。
 * 非 Web 环境（无 {@link HttpServletRequest}）时使用回退字符串。</p>
 */
public class RequestFingerprintBuilder {

  /** Authorization 请求头名称。 */
  private static final String AUTHORIZATION = "Authorization";

  /** token 请求头名称。 */
  private static final String TOKEN = "token";

  /** access_token 请求头名称。 */
  private static final String ACCESS_TOKEN = "access_token";

  /**
   * 生成指纹，非 Web 环境使用默认回退。
   */
  public String build() {
    return build("none-web-request");
  }

  /**
   * 生成请求指纹：对 {HTTP方法:URL:token} 进行 SHA-256。
   *
   * @param fallback 非 Web 环境下的回退字符串
   * @return 指纹字符串
   */
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

  /** 提取完整请求 URL，含查询参数。 */
  private String requestUrl(HttpServletRequest request) {
    String queryString = request.getQueryString();
    if (queryString == null || queryString.isBlank()) {
      return request.getRequestURL().toString();
    }
    return request.getRequestURL() + "?" + queryString;
  }

  /**
   * 从请求头中提取用户 token，按优先级依次尝试：
   * Authorization → token → access_token。
   */
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

  /** 判断字符串是否为非空白。 */
  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

}
