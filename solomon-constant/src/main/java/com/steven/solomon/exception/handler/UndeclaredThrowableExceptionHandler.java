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

  /**
   * 处理 JDK 动态代理包装异常，还原内部真实异常的错误信息。
   *
   * <p>如果内部异常是 {@link BaseException}，还原业务错误码和消息；
   * 否则返回通用系统异常编码。</p>
   *
   * @param ex 捕获的 UndeclaredThrowableException 异常
   * @return 包含真实异常信息的异常响应体
   */
  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    Throwable target = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
    if (target instanceof BaseException baseException) {
      return codeResponse(baseException.getCode(), baseException.getMessage(), 500);
    }
    return codeResponse(BaseExceptionCode.BASE_EXCEPTION_CODE, 500);
  }
}
