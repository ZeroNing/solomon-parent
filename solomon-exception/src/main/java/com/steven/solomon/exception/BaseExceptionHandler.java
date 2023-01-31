package com.steven.solomon.exception;

import com.steven.solomon.vo.BaseExceptionVO;
import org.springframework.stereotype.Component;

@Component("BaseExceptionProcessor")
public class BaseExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    BaseException e = (BaseException) ex;
    return new BaseExceptionVO(e.getCode(), e.getMessage(), 500);
  }
}
