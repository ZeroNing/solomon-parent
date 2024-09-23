package com.steven.solomon.exception;

import com.steven.solomon.utils.i18n.I18nUtils;

public class BaseException extends Exception {

  private static final long serialVersionUID = -5121152313724499190L;

  protected String code;

  protected Object[] args;

  public BaseException(String code) {
    super(I18nUtils.getErrorMessage(code));
    this.code = code;
  }

  public BaseException(String code, Object... args) {
    super(I18nUtils.getErrorMessage(code,args));
    this.code = code;
    this.args = args;
  }

  public BaseException(String code, Throwable e, Object... args) {
    super(I18nUtils.getErrorMessage(code,args),e);
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
