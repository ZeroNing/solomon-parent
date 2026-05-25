package com.steven.solomon.pojo.vo;

import com.steven.solomon.code.BaseCode;
import java.io.Serializable;

/**
 * 接口统一返回对象。
 *
 * <p>默认表示成功响应，异常响应由 {@link BaseExceptionVO} 承载。</p>
 */
public class ResultVO<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int SUCCESS_STATUS = 200;

  /**
   * 响应提示信息。
   */
  private String message;

  /**
   * HTTP 风格状态码。
   */
  private Integer status;

  /**
   * 业务数据。
   */
  private T data;

  public static <T> ResultVO<T> success(T data) {
    return new ResultVO<>(data);
  }

  public static <T> ResultVO<T> success() {
    return new ResultVO<>();
  }

  public ResultVO() {
    this(null);
  }

  public ResultVO(T data) {
    this.message = BaseCode.DEFAULT_SUCCESS_PHRASE;
    this.status = SUCCESS_STATUS;
    this.data = data;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
