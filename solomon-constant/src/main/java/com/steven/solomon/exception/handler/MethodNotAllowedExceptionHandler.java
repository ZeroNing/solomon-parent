package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods=false,value = "MethodNotAllowedExceptionProcessor")
public class MethodNotAllowedExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return new BaseExceptionVO(BaseExceptionCode.REQUEST_METHOD_ERROR, 500);
  }
}
