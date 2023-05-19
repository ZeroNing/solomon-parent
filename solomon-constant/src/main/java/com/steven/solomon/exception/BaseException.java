package com.steven.solomon.exception;

import com.steven.solomon.utils.I18nUtils;

public class BaseException extends Exception {

  private static final long serialVersionUID = -5121152313724499190L;

  protected String code;

  protected String[] args;

  public BaseException(String code, String message){
    super(message);
    this.code = code;
  }

  public BaseException(String code) {
    super(I18nUtils.getErrorMessage(code));
    this.code = code;
  }

  public BaseException(String code, String... args) {
    super(I18nUtils.getErrorMessage(code,args));
    this.code = code;
    this.args = args;
  }

  public BaseException(String code, Throwable e, String... args) {
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

  public String[] getArgs() {
    return args;
  }

}
