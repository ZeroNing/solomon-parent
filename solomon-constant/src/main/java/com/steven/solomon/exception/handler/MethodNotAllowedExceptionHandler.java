package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 通用“请求方法不允许”异常处理器。
 */
@Configuration(proxyBeanMethods = false, value = "MethodNotAllowedExceptionProcessor")
@ConditionalOnMissingBean(name = "MethodNotAllowedExceptionProcessor")
public class MethodNotAllowedExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.REQUEST_METHOD_ERROR, 405);
  }
}
