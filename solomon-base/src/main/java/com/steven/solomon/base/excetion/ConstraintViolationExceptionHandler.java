package com.steven.solomon.base.excetion;

import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import javax.validation.ConstraintViolationException;
import org.springframework.stereotype.Component;

@Component("ConstraintViolationExceptionProcessor")
public class ConstraintViolationExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    ConstraintViolationException e = (ConstraintViolationException) ex;
    return new BaseExceptionVO(e.getMessage(), 500);
  }
}
