package com.steven.solomon.exception;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.vo.BaseExceptionVO;
import org.springframework.stereotype.Component;

@Component("ResponseStatusExceptionProcessor")
public class ResponseStatusExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    return new BaseExceptionVO(BaseExceptionCode.BAD_REQUEST,500);
  }

}
