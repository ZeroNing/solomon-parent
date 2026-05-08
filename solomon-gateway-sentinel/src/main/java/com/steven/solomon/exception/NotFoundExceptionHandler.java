package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;

/**
 * 服务不存在异常处理器
 * 当网关路由不到后端服务或服务实例不存在时，统一处理并返回友好响应
 *
 * @author steven
 * @since 1.0.0
 */
@Configuration(value = "NotFoundExceptionProcessor", proxyBeanMethods = false)
public class NotFoundExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理服务不存在异常
   * 解析异常信息，返回404状态码和服务调用失败提示
   *
   * @param ex 异常对象
   * @return 标准化异常响应对象
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    BaseExceptionVO baseExceptionVO = new BaseExceptionVO(BaseExceptionCode.BASE_EXCEPTION_CODE, 404);
    
    // 如果是网关找不到服务的异常
    if (ex instanceof NotFoundException) {
      NotFoundException notFoundEx = (NotFoundException) ex;
      String reason = notFoundEx.getReason();
      // 提取请求的服务路径信息
      reason = reason.substring(reason.lastIndexOf("for ") + 4);
      
      // 构造服务调用错误响应
      baseExceptionVO = new BaseExceptionVO(
          BaseExceptionCode.SERVICE_CALL_ERROR, 
          notFoundEx.getStatusCode().value()
      );
      // 将服务路径作为参数返回，方便定位问题
      baseExceptionVO.setArg(reason);
    }
    return baseExceptionVO;
  }

}
