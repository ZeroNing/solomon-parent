package com.steven.solomon.base.exception;

import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 处理方法参数上的 Jakarta Validation 校验异常。
 *
 * <p>Bean 名称必须保持为 {@code ConstraintViolationExceptionProcessor}，
 * {@link com.steven.solomon.exception.ExceptionUtil} 会按
 * “异常简单类名 + Processor”的规则查找处理器。</p>
 */
@Configuration(proxyBeanMethods = false, value = "ConstraintViolationExceptionProcessor")
@ConditionalOnMissingBean(name = "ConstraintViolationExceptionProcessor")
public class ConstraintViolationExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理 Jakarta Validation 校验异常，提取异常消息并以 400 状态码返回。
   *
   * @param ex 捕获的 ConstraintViolationException 异常
   * @return 包含校验错误消息和状态码的异常响应体
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    ConstraintViolationException exception = (ConstraintViolationException) ex;
    return messageResponse(exception.getMessage(), 400);
  }
}
