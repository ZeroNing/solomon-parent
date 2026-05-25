package com.steven.solomon.pojo.vo;

import java.io.Serializable;
import java.util.Locale;

/**
 * 统一异常响应对象。
 *
 * <p>该对象是异常处理器内部的标准模型，最终会被转换成 Map 返回给前端。
 * 保留 locale、arg 等字段，是为了在统一异常处理阶段可以继续补全国际化文案。</p>
 */
public class BaseExceptionVO implements Serializable {

  private static final long serialVersionUID = 712433877274412890L;

  /**
   * 业务错误码。
   */
  private String code;

  /**
   * 可展示的错误消息。
   */
  private String message;

  /**
   * HTTP 状态码。
   */
  private Integer statusCode;

  /**
   * 服务标识。
   */
  private String serverId;

  /**
   * 当前请求语言环境。
   */
  private Locale locale;

  /**
   * 国际化消息参数。
   */
  private String arg;

  /**
   * 请求链路 ID。
   */
  private String requestId;

  public BaseExceptionVO() {
  }

  public BaseExceptionVO(String code, String message, int statusCode) {
    this.code = code;
    this.message = message;
    this.statusCode = statusCode;
  }

  public BaseExceptionVO(String code, int statusCode) {
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
    return statusCode;
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

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getArg() {
    return arg;
  }

  public void setArg(String arg) {
    this.arg = arg;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
}
