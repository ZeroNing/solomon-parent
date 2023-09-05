package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "ResponseStatusExceptionProcessor",proxyBeanMethods = false)
public class ResponseStatusExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return new BaseExceptionVO(BaseExceptionCode.BAD_REQUEST,500);
  }

}
