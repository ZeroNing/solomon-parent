package com.steven.solomon.datasource.exception;

import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import org.springframework.context.annotation.Configuration;

/**
 * 动态数据源异常处理器。
 *
 * <p>数据源模块会抛出 {@link DataSourceException}，该异常已经携带业务错误码和明确消息。
 * 这里复用基类响应构建方法，避免重复创建异常响应对象。</p>
 */
@Configuration(proxyBeanMethods = false, value = "DataSourceExceptionProcessor")
public class DataSourceExceptionHandler extends AbstractExceptionHandler {

  @Override
  public BaseExceptionVO handleBaseException(Throwable ex) {
    DataSourceException exception = (DataSourceException) ex;
    return codeResponse(exception.getCode(), exception.getMessage(), 500);
  }
}
