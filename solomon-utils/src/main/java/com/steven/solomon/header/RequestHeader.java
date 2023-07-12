package com.steven.solomon.header;

import java.io.Serializable;

public class RequestHeader implements Serializable {

  private String serverTimeZone;

  public String getServerTimeZone() {
    return serverTimeZone;
  }

  public void setServerTimeZone(String serverTimeZone) {
    this.serverTimeZone = serverTimeZone;
  }
}
