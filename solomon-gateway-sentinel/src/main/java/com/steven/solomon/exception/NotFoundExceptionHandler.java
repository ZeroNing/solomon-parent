package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "NotFoundExceptionProcessor",proxyBeanMethods = false)
public class NotFoundExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    BaseExceptionVO baseExceptionVO = new BaseExceptionVO(BaseExceptionCode.BASE_EXCEPTION_CODE, 404);
    if(ex instanceof NotFoundException){
      String reason = ((NotFoundException) ex).getReason();
      reason = reason.substring(reason.lastIndexOf("for ")+4);
      baseExceptionVO = new BaseExceptionVO(BaseExceptionCode.SERVICE_CALL_ERROR,((NotFoundException) ex).getStatusCode().value());
      baseExceptionVO.setArg(reason);
    }
    return baseExceptionVO;
  }

}
