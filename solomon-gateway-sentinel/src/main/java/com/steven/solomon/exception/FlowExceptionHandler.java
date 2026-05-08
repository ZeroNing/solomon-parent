package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 流量控制异常处理器
 * 当请求触发限流规则时，统一处理并返回友好响应
 *
 * @author steven
 * @since 1.0.0
 */
@Configuration(value = "FlowExceptionProcessor", proxyBeanMethods = false)
public class FlowExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理流量控制异常
   * 返回429请求过多状态码和系统限流提示信息
   *
   * @param ex 异常对象
   * @return 标准化异常响应对象
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    // 返回系统限流错误码和429请求过多HTTP状态
    return new BaseExceptionVO(BaseExceptionCode.SYSTEM_LIMITING, 429);
  }
}
