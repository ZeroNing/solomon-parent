package com.steven.solomon.exception;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.vo.BaseExceptionVO;
import org.springframework.stereotype.Component;

@Component("DegradeExceptionProcessor")
public class DegradeExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    return new BaseExceptionVO(BaseExceptionCode.SYSTEM_FUSING,500);
  }
}
