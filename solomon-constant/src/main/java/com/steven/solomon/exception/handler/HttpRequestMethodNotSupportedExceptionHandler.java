package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP 请求方法不支持异常处理器。
 */
@Configuration(proxyBeanMethods = false, value = "HttpRequestMethodNotSupportedExceptionProcessor")
@ConditionalOnMissingBean(name = "HttpRequestMethodNotSupportedExceptionProcessor")
public class HttpRequestMethodNotSupportedExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return new BaseExceptionVO(BaseExceptionCode.REQUEST_METHOD_ERROR, 405);
  }
}
