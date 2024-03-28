package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "FlowExceptionProcessor",proxyBeanMethods = false)
public class FlowExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return new BaseExceptionVO(BaseExceptionCode.SYSTEM_LIMITING,429);
  }
}
