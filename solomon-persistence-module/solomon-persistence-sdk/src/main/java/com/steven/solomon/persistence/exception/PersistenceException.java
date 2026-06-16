package com.steven.solomon.persistence.exception;

import com.steven.solomon.exception.BaseException;

/**
 * 数据源模块基础异常�?
 *
 * <p>所有可预期的数据源错误都通过BaseException体系抛出，方便统一异常处理器读取国际化文案�?/p>
 */
public class PersistenceException extends BaseException {

  private static final long serialVersionUID = 617464254097787755L;

  public PersistenceException(String code, Object... args) {
    super(code, args);
  }

  public PersistenceException(String code, Throwable e, Object... args) {
    super(code, e, args);
  }
}
