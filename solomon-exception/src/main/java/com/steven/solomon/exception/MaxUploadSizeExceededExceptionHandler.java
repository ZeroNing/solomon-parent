package com.steven.solomon.exception;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.vo.BaseExceptionVO;
import org.springframework.stereotype.Component;

@Component("MaxUploadSizeExceededExceptionProcessor")
public class MaxUploadSizeExceededExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Exception ex) {
    return new BaseExceptionVO(BaseExceptionCode.FILE_UPLOAD_MAX_SIZE,500);
  }
}
