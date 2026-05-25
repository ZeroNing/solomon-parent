package com.steven.solomon.exception;

import com.steven.solomon.utils.i18n.I18nUtils;

/**
 * Solomon 业务异常基类。
 *
 * <p>业务代码需要主动中断流程并返回标准错误响应时，推荐抛出该异常。
 * 异常消息会在构造时通过错误码做国际化解析，统一异常处理器也会继续保留错误码，
 * 方便前端或调用方做精确判断。</p>
 */
public class BaseException extends Exception {

  private static final long serialVersionUID = -5121152313724499190L;

  /**
   * 业务错误码，对应 i18n/messages_*.properties 中的错误文案。
   */
  protected String code;

  /**
   * 国际化消息占位参数。
   */
  protected Object[] args;

  public BaseException(String code) {
    super(I18nUtils.getErrorMessage(code));
    this.code = code;
  }

  public BaseException(String code, Object... args) {
    super(I18nUtils.getErrorMessage(code, args));
    this.code = code;
    this.args = args;
  }

  public BaseException(String code, Throwable ex, Object... args) {
    super(I18nUtils.getErrorMessage(code, args), ex);
    this.code = code;
    this.args = args;
  }

  public BaseException(Throwable cause) {
    super(cause);
  }

  public String getCode() {
    return code;
  }

  public Object[] getArgs() {
    return args;
  }
}
