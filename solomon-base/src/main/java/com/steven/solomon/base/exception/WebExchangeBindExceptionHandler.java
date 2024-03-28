package com.steven.solomon.base.exception;

import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.verification.ValidateUtils;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;

@Configuration(value = "WebExchangeBindExceptionProcessor",proxyBeanMethods = false)
public class WebExchangeBindExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    List<ObjectError> errorMessages = ((WebExchangeBindException) ex).getAllErrors();
    String            errorMessage  = null;
    if (ValidateUtils.isNotEmpty(errorMessages)) {
      ObjectError objectError = errorMessages.get(0);
      errorMessage = objectError.getDefaultMessage();
    }
    return new BaseExceptionVO(errorMessage, 400);
  }
}
