package com.steven.solomon.vo;


import java.io.Serializable;
import java.util.Locale;

public class BaseExceptionVO implements Serializable {

  /**
   * 异常编码
   */
  String code;
  /**
   * 异常语句
   */
  String message;

  /**
   * 状态编码
   */
  Integer statusCode;

  /**
   * 服务id
   */
  String serverId;
  /**
   * 国际化语言
   */
  Locale locale;

  /**
   * 国家化参数
   */
  String arg;

  public BaseExceptionVO(){
    super();
  }

  public BaseExceptionVO(String code, String message,int statusCode){
    super();
    this.code = code;
    this.message = message;
    this.statusCode = statusCode;
  }

  public BaseExceptionVO(String code,int statusCode){
    super();
    this.code = code;
    this.statusCode = statusCode;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getStatusCode() {
//    return ValidateUtils.isEmpty(this.statusCode) ? HttpStatus.INTERNAL_SERVER_ERROR.value() : this.statusCode;
    return 500;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public void setArg(String arg) {
    this.arg = arg;
  }

  public Locale getLocale() {
    return locale;
  }

  public String getArg() {
    return arg;
  }
}
