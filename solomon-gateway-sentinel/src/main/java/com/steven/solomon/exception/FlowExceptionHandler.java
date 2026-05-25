package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 限流异常处理器。
 *
 * <p>当请求触发 Sentinel 流控规则时，统一返回系统限流错误码和 429 状态码。
 * 响应对象通过基类方法构建，避免各异常处理器重复创建 {@link BaseExceptionVO}。</p>
 */
@Configuration(value = "FlowExceptionProcessor", proxyBeanMethods = false)
public class FlowExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    return codeResponse(BaseExceptionCode.SYSTEM_LIMITING, 429);
  }
}
