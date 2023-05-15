package com.steven.solomon.exception.handler;

import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

@Configuration(proxyBeanMethods=false,value = "BindExceptionProcessor")
public class BindExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    BindException e = (BindException) ex;
    return new BaseExceptionVO(e.getFieldError().getDefaultMessage(), 500);
  }
}
