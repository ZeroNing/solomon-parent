package com.steven.solomon.exception.handler;

import com.steven.solomon.pojo.vo.BaseExceptionVO;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractExceptionHandler {

  public static final String HANDLER_NAME = "Processor";

  public static Map<String, AbstractExceptionHandler> exceptionHandlerMap = null;

  /**
   * 根据不同的异常返回不同的异常信息
   * @return 异常对象
   */
  public abstract BaseExceptionVO handleBaseException(Throwable ex);
}
