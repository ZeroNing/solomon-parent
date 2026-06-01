package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 熔断降级异常处理器。
 *
 * <p>当后端服务触发熔断或降级规则时，统一返回系统熔断错误码和 503 状态码。</p>
 */
@Configuration(value = "DegradeExceptionProcessor", proxyBeanMethods = false)
public class DegradeExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.SYSTEM_FUSING, 503);
  }
}
