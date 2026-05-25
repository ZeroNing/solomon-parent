package com.steven.solomon.http;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.steven.solomon.verification.ValidateUtils;

/**
 * HTTP 请求工具类。
 *
 * <p>在 Hutool {@link HttpUtil} 基础上补充项目常用的请求初始化方法。</p>
 */
public class HttpUtils extends HttpUtil {

  private HttpUtils() {}

  /**
   * 创建 HTTP 请求，并在传入 contentType 时设置请求类型。
   */
  public static HttpRequest initRequest(Method method, String url, ContentType contentType) {
    HttpRequest request = createRequest(method, url);
    if (ValidateUtils.isNotEmpty(contentType)) {
      request.contentType(contentType.getValue());
    }
    return request;
  }

  /**
   * 创建不指定 contentType 的 HTTP 请求。
   */
  public static HttpRequest initRequest(Method method, String url) {
    return initRequest(method, url, null);
  }
}
