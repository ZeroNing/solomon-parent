package com.steven.solomon.base.exception;

import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Configuration(proxyBeanMethods = false,value = "MethodArgumentNotValidExceptionProcessor")
public class MethodArgumentNotValidExceptionHandler extends AbstractExceptionHandler {

  private Logger logger = LoggerUtils.logger(getClass());

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    MethodArgumentNotValidException e = (MethodArgumentNotValidException) ex;
    String message = e.getBindingResult().getFieldError().getDefaultMessage();
    return new BaseExceptionVO(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
  }
}
