package com.steven.solomon.exception.handler;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import java.lang.reflect.UndeclaredThrowableException;
import org.springframework.stereotype.Component;

@Component("UndeclaredThrowableExceptionProcessor")
public class UndeclaredThrowableExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    Throwable t = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
    if(t instanceof BaseException){
      BaseException baseException = (BaseException) t;
      return new BaseExceptionVO(baseException.getCode(),baseException.getMessage(),500);
    }
    return new BaseExceptionVO(BaseExceptionCode.BASE_EXCEPTION_CODE,500);
  }
}
