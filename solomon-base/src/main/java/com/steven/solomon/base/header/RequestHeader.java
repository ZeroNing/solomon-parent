package com.steven.solomon.base.header;

import java.io.Serializable;

public class RequestHeader implements Serializable {

  private String token;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
