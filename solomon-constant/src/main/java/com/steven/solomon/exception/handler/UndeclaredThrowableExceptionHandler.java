package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.UndeclaredThrowableException;

@Configuration(proxyBeanMethods=false,value = "UndeclaredThrowableExceptionProcessor")
public class UndeclaredThrowableExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    Throwable t = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
    if(t instanceof BaseException){
      BaseException baseException = (BaseException) t;
      return new BaseExceptionVO(baseException.getCode(),baseException.getMessage(),500);
    }
    return new BaseExceptionVO(BaseExceptionCode.BASE_EXCEPTION_CODE,500);
  }
}
