package com.steven.solomon.constant.pojo.vo;

import com.steven.solomon.constant.code.BaseCode;
import java.io.Serializable;

public class ResultVO<T> implements Serializable {

  private String message;

  private Integer status;

  private T data;

  public static<T> ResultVO<T> success(T data){
    return new ResultVO<>(data);
  }

  public static<T> ResultVO<T> success(){
    return new ResultVO<>();
  }

  public ResultVO(T data){
    super();
    this.message = BaseCode.DEFAULT_SUCCESS_PHRASE;
    this.data = data;
    this.status = 200;
  }

  public ResultVO(){
    super();
    this.message = BaseCode.DEFAULT_SUCCESS_PHRASE;
    this.data = null;
    this.status = 200;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
