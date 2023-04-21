package com.steven.solomon.exception.handler;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import org.springframework.stereotype.Component;

@Component("HttpRequestMethodNotSupportedExceptionProcessor")
public class HttpRequestMethodNotSupportedExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    return new BaseExceptionVO(BaseExceptionCode.REQUEST_METHOD_ERROR, 500);
  }
}
