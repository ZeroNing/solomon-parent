package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;

/**
 * 响应状态异常处理器。
 *
 * <p>处理网关层无法细分的响应状态异常，默认按请求错误返回 404。</p>
 */
@Configuration(value = "ResponseStatusExceptionProcessor", proxyBeanMethods = false)
public class ResponseStatusExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.BAD_REQUEST, 404);
  }
}
