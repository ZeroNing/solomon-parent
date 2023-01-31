package com.steven.solomon.exception;

import com.steven.solomon.vo.BaseExceptionVO;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

@Component("BindExceptionProcessor")
public class BindExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    BindException e = (BindException) ex;
    return new BaseExceptionVO(e.getFieldError().getDefaultMessage(), 500);
  }
}
