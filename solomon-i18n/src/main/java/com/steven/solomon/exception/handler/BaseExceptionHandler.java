package com.steven.solomon.exception.handler;

import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.BaseException;
import org.springframework.stereotype.Component;

@Component("BaseExceptionProcessor")
public class BaseExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    BaseException e = (BaseException) ex;
    return new BaseExceptionVO(e.getCode(), e.getMessage(), 500);
  }
}
