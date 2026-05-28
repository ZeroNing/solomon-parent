package com.steven.solomon.exception.handler;

import com.steven.solomon.pojo.vo.BaseExceptionVO;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

/**
 * 表单或对象绑定异常默认处理器。
 *
 * <p>优先返回第一个字段错误，便于前端直接提示；如果没有字段错误，则回退到异常原始消息。</p>
 */
@Configuration(proxyBeanMethods = false, value = "BindExceptionProcessor")
@ConditionalOnMissingBean(name = "BindExceptionProcessor")
public class BindExceptionHandler extends AbstractExceptionHandler {

  /**
   * 处理表单绑定异常，优先提取第一个字段错误信息。
   *
   * @param ex 捕获的 BindException 异常
   * @return 包含校验错误消息的异常响应体
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    BindException exception = (BindException) ex;
    FieldError fieldError = exception.getFieldError();
    String message = ObjectUtil.isEmpty(fieldError)
        ? exception.getMessage()
        : fieldError.getDefaultMessage();
    return messageResponse(message, 400);
  }
}
