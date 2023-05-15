package com.steven.solomon.exception;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration(value = "DegradeExceptionProcessor",proxyBeanMethods = false)
public class DegradeExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    return new BaseExceptionVO(BaseExceptionCode.SYSTEM_FUSING,500);
  }
}
