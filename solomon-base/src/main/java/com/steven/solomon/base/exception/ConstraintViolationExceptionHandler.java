package com.steven.solomon.base.exception;

import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import javax.validation.ConstraintViolationException;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false,value = "ConstraintViolationExceptionProcessor")
public class ConstraintViolationExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    ConstraintViolationException e = (ConstraintViolationException) ex;
    return new BaseExceptionVO(e.getMessage(), 500);
  }
}
