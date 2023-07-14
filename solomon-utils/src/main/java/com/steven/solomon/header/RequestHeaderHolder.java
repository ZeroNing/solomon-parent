package com.steven.solomon.header;

import com.steven.solomon.verification.ValidateUtils;
import java.time.ZoneId;

public class RequestHeaderHolder {

  protected static final ThreadLocal<RequestHeader> threadLocal = ThreadLocal.withInitial(() -> {
    RequestHeader header = new RequestHeader();
    return header;
  });

  public static String getTimeZone(){
    String serverTimeZone = threadLocal.get().getTimezone();
    serverTimeZone = ValidateUtils.getOrDefault(serverTimeZone,ZoneId.systemDefault().getId());
    try {
      ZoneId.of(serverTimeZone);
    } catch (Exception e){
      serverTimeZone = ZoneId.systemDefault().getId();
    }
    return serverTimeZone;
  }
  public static void setTimeZone(String serverTimeZone) {
    threadLocal.get().setTimezone(serverTimeZone);
  }
}
