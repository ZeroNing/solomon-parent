package com.steven.solomon.base.model;

import com.steven.solomon.constant.code.BaseCode;
import java.io.Serializable;
import org.springframework.http.HttpStatus;

public class Result<T> implements Serializable {

  private String message;

  private Integer status;

  private T data;

  public static<T> Result<T> success(T data){
    return new Result<>(data);
  }

  public static<T> Result<T> success(){
    return new Result<>();
  }

  public Result(T data){
    super();
    this.message = BaseCode.DEFAULT_SUCCESS_PHRASE;
    this.data = data;
    this.status = HttpStatus.OK.value();
  }

  public Result(){
    super();
    this.message = BaseCode.DEFAULT_SUCCESS_PHRASE;
    this.data = null;
    this.status = HttpStatus.OK.value();
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
