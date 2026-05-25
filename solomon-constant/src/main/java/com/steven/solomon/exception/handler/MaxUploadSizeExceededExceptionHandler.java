package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 上传文件超过最大限制异常处理器。
 */
@Configuration(proxyBeanMethods = false, value = "MaxUploadSizeExceededExceptionProcessor")
@ConditionalOnMissingBean(name = "MaxUploadSizeExceededExceptionProcessor")
public class MaxUploadSizeExceededExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.FILE_UPLOAD_MAX_SIZE, 413);
  }
}
