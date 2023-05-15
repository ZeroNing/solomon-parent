package com.steven.solomon.exception.handler;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.constant.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration(proxyBeanMethods=false,value = "MaxUploadSizeExceededExceptionProcessor")
public class MaxUploadSizeExceededExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    return new BaseExceptionVO(BaseExceptionCode.FILE_UPLOAD_MAX_SIZE,500);
  }
}
