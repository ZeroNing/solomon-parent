package com.steven.solomon.exception;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration(value = "ResponseStatusExceptionProcessor",proxyBeanMethods = false)
public class ResponseStatusExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    return new BaseExceptionVO(BaseExceptionCode.BAD_REQUEST,500);
  }

}
