package com.steven.solomon.header;

import java.io.Serializable;

public class RequestHeader implements Serializable {

  private String timezone;

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }
}
