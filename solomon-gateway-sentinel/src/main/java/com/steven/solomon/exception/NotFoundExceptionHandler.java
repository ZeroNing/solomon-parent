package com.steven.solomon.exception;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.vo.BaseExceptionVO;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;

@Component("NotFoundExceptionProcessor")
public class NotFoundExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    BaseExceptionVO baseExceptionVO = new BaseExceptionVO(BaseExceptionCode.BASE_EXCEPTION_CODE, 500);
    if(ex instanceof NotFoundException){
      String reason = ((NotFoundException) ex).getReason();
      reason = reason.substring(reason.lastIndexOf("for ")+4);
      baseExceptionVO = new BaseExceptionVO(BaseExceptionCode.SERVICE_CALL_ERROR,((NotFoundException) ex).getStatus().value());
      baseExceptionVO.setArg(reason);
    } else {
      return baseExceptionVO;
    }
    return baseExceptionVO;
  }

}
