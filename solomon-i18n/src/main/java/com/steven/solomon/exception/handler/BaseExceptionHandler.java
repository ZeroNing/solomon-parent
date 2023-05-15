package com.steven.solomon.exception.handler;

import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.BaseException;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration(proxyBeanMethods=false,value = "BaseExceptionProcessor")
public class BaseExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    BaseException e = (BaseException) ex;
    return new BaseExceptionVO(e.getCode(), e.getMessage(), 500);
  }
}
