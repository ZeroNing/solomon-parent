package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import org.springframework.context.annotation.Configuration;

/**
 * 响应状态异常处理器
 * 处理@ResponseStatus注解标注的异常和HTTP状态异常
 *
 * @author steven
 * @since 1.0.0
 */
@Configuration(value = "ResponseStatusExceptionProcessor", proxyBeanMethods = false)
public class ResponseStatusExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理响应状态异常
   * 返回404状态码和请求错误提示信息
   *
   * @param ex 异常对象
   * @return 标准化异常响应对象
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    // 返回请求错误码和404资源不存在HTTP状态
    return new BaseExceptionVO(BaseExceptionCode.BAD_REQUEST, 404);
  }

}
