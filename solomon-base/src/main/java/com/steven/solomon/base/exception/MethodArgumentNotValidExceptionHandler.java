package com.steven.solomon.base.exception;

import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * 处理 {@code @Valid} / {@code @Validated} 触发的 Spring MVC 参数校验异常。
 *
 * <p>优先返回第一个字段错误，便于前端直接展示；如果没有字段错误，则回退到异常原始消息。
 * Bean 名称保持为 {@code MethodArgumentNotValidExceptionProcessor}，兼容统一异常路由。</p>
 */
@Configuration(proxyBeanMethods = false, value = "MethodArgumentNotValidExceptionProcessor")
@ConditionalOnMissingBean(name = "MethodArgumentNotValidExceptionProcessor")
public class MethodArgumentNotValidExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    MethodArgumentNotValidException exception = (MethodArgumentNotValidException) ex;
    FieldError fieldError = exception.getBindingResult().getFieldError();
    String message = fieldError == null ? exception.getMessage() : fieldError.getDefaultMessage();
    return new BaseExceptionVO(null, message, 400);
  }
}
