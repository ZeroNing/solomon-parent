package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP 请求方法不支持异常处理器。
 *
 * <p>当客户端使用了服务器不支持的 HTTP 方法时，返回 405 状态码和请求方法错误编码。</p>
 */
@Configuration(proxyBeanMethods = false, value = "HttpRequestMethodNotSupportedExceptionProcessor")
@ConditionalOnMissingBean(name = "HttpRequestMethodNotSupportedExceptionProcessor")
public class HttpRequestMethodNotSupportedExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理 HTTP 请求方法不支持异常，返回 405 状态码。
   *
   * @param ex 捕获的 HttpRequestMethodNotSupportedException 异常
   * @return 包含请求方法错误编码的异常响应体
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.REQUEST_METHOD_ERROR, 405);
  }
}
