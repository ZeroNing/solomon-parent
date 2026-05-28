package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 通用“请求方法不允许”异常处理器。
 *
 * <p>与 {@link HttpRequestMethodNotSupportedExceptionHandler} 类似，返回 405 状态码。</p>
 */
@Configuration(proxyBeanMethods = false, value = "MethodNotAllowedExceptionProcessor")
@ConditionalOnMissingBean(name = "MethodNotAllowedExceptionProcessor")
public class MethodNotAllowedExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理请求方法不允许异常，返回 405 状态码。
   *
   * @param ex 捕获的 MethodNotAllowedException 异常
   * @return 包含请求方法错误编码的异常响应体
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.REQUEST_METHOD_ERROR, 405);
  }
}
