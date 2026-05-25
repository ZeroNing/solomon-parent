package com.steven.solomon.exception.handler;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import java.lang.reflect.UndeclaredThrowableException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * JDK 动态代理包装异常处理器。
 *
 * <p>反射或代理调用可能把真实异常包进 {@link UndeclaredThrowableException}。
 * 如果内部真实异常是 {@link BaseException}，这里会还原业务错误码和消息。</p>
 */
@Configuration(proxyBeanMethods = false, value = "UndeclaredThrowableExceptionProcessor")
@ConditionalOnMissingBean(name = "UndeclaredThrowableExceptionProcessor")
public class UndeclaredThrowableExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    Throwable target = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
    if (target instanceof BaseException baseException) {
      return codeResponse(baseException.getCode(), baseException.getMessage(), 500);
    }
    return codeResponse(BaseExceptionCode.BASE_EXCEPTION_CODE, 500);
  }
}
