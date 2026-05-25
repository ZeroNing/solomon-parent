package com.steven.solomon.exception.handler;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 业务异常默认处理器。
 *
 * <p>业务代码主动抛出的 {@link BaseException} 会进入这里，错误码和消息都直接来自异常本身。
 * Bean 名称必须保持为 {@code BaseExceptionProcessor}，统一异常工具会按名称查找处理器。</p>
 */
@Configuration(proxyBeanMethods = false, value = "BaseExceptionProcessor")
@ConditionalOnMissingBean(name = "BaseExceptionProcessor")
public class BaseExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    BaseException exception = (BaseException) ex;
    return new BaseExceptionVO(exception.getCode(), exception.getMessage(), 500);
  }
}
