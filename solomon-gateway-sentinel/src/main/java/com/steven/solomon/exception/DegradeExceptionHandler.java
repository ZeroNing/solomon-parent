package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 服务降级异常处理器
 * 当后端服务触发熔断降级规则时，统一处理并返回友好响应
 *
 * @author steven
 * @since 1.0.0
 */
@Configuration(value = "DegradeExceptionProcessor", proxyBeanMethods = false)
public class DegradeExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理服务降级异常
   * 返回503服务不可用状态码和系统熔断提示信息
   *
   * @param ex 异常对象
   * @return 标准化异常响应对象
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    // 返回系统熔断错误码和503服务不可用HTTP状态
    return new BaseExceptionVO(BaseExceptionCode.SYSTEM_FUSING, 503);
  }
}
