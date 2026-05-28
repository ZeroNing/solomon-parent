package com.steven.solomon.base.exception;

import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.verification.ValidateUtils;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * 处理 WebFlux 绑定异常，保留给引入响应式 Web 模块的业务使用。
 *
 * <p>虽然 solomon-common 以 Spring MVC 为主，但基础异常模块可以同时提供
 * WebFlux 的异常翻译能力；业务侧如果声明同名 Bean，会自动覆盖该默认实现。</p>
 */
@Configuration(proxyBeanMethods = false, value = "WebExchangeBindExceptionProcessor")
@ConditionalOnMissingBean(name = "WebExchangeBindExceptionProcessor")
public class WebExchangeBindExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理 WebFlux 绑定异常，提取第一个校验错误信息。
   *
   * <p>若无校验错误，则回退到异常原始消息。</p>
   *
   * @param ex 捕获的 WebExchangeBindException 异常
   * @return 包含校验错误消息和状态码的异常响应体
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    WebExchangeBindException exception = (WebExchangeBindException) ex;
    List<ObjectError> errors = exception.getAllErrors();
    String message = ValidateUtils.isEmpty(errors)
        ? exception.getMessage()
        : errors.get(0).getDefaultMessage();
    return messageResponse(message, 400);
  }
}
