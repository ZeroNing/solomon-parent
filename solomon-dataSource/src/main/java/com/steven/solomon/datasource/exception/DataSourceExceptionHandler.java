package com.steven.solomon.datasource.exception;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false,value = "DataSourceExceptionProcessor")
public class DataSourceExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    DataSourceException e = (DataSourceException) ex;
    return new BaseExceptionVO(e.getCode(), e.getMessage(), 500);
  }
}
