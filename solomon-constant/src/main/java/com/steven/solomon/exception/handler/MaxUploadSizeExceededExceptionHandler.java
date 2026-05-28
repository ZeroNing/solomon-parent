package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 上传文件超过最大限制异常处理器。
 *
 * <p>当上传文件大小超过配置的最大限制时，返回 413 状态码和文件大小超限编码。</p>
 */
@Configuration(proxyBeanMethods = false, value = "MaxUploadSizeExceededExceptionProcessor")
@ConditionalOnMissingBean(name = "MaxUploadSizeExceededExceptionProcessor")
public class MaxUploadSizeExceededExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理上传文件超限异常，返回 413 状态码。
   *
   * @param ex 捕获的 MaxUploadSizeExceededException 异常
   * @return 包含文件大小超限编码的异常响应体
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.FILE_UPLOAD_MAX_SIZE, 413);
  }
}
