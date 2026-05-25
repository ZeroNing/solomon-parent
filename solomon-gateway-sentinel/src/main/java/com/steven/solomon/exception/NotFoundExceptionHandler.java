package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;

/**
 * 网关服务未找到异常处理器。
 *
 * <p>当 Gateway 找不到后端服务实例时，优先返回服务调用失败错误码，并把服务路径作为
 * 国际化参数写入响应对象，方便定位是哪一个下游服务不可用。</p>
 */
@Configuration(value = "NotFoundExceptionProcessor", proxyBeanMethods = false)
public class NotFoundExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    if (!(ex instanceof NotFoundException notFoundException)) {
      return codeResponse(BaseExceptionCode.BASE_EXCEPTION_CODE, 404);
    }

    BaseExceptionVO response = codeResponse(
        BaseExceptionCode.SERVICE_CALL_ERROR,
        notFoundException.getStatusCode().value());
    response.setArg(resolveServiceName(notFoundException.getReason()));
    return response;
  }

  /**
   * 从 Gateway 的异常 reason 中提取服务标识。
   *
   * <p>常见 reason 格式包含 “for xxx”，没有匹配到时直接返回原始 reason。</p>
   */
  private String resolveServiceName(String reason) {
    if (reason == null || reason.isEmpty()) {
      return "";
    }
    int index = reason.lastIndexOf("for ");
    return index < 0 ? reason : reason.substring(index + 4);
  }
}
