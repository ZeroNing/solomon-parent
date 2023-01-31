package com.steven.solomon.base.header;

public class RequestHeaderHolder {

  private static final ThreadLocal<RequestHeader> threadLocal = ThreadLocal.withInitial(() -> {
    RequestHeader header = new RequestHeader();
    header.setToken("DEFAULT");
    return header;
  });

  public static String getToken() {
    return threadLocal.get().getToken();
  }

  public static void setToken(String token) {
    threadLocal.get().setToken(token);
  }

}
