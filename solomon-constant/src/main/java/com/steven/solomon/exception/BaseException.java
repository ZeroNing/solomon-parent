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

  /**
   * 使用错误码构造异常，自动通过国际化工具解析消息。
   *
   * @param code 业务错误码
   */
  public BaseException(String code) {
    super(I18nUtils.getErrorMessage(code));
    this.code = code;
  }

  /**
   * 使用错误码和占位参数构造异常，自动通过国际化工具解析消息。
   *
   * @param code 业务错误码
   * @param args 国际化消息占位参数
   */
  public BaseException(String code, Object... args) {
    super(I18nUtils.getErrorMessage(code, args));
    this.code = code;
    this.args = args;
  }

  /**
   * 使用错误码、原始异常和占位参数构造异常。
   *
   * @param code 业务错误码
   * @param ex   原始异常，保留异常链
   * @param args 国际化消息占位参数
   */
  public BaseException(String code, Throwable ex, Object... args) {
    super(I18nUtils.getErrorMessage(code, args), ex);
    this.code = code;
    this.args = args;
  }

  /**
   * 直接使用原始异常构造，不指定错误码。
   *
   * @param cause 原始异常
   */
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
