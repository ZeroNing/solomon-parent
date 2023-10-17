package com.steven.solomon.holder;

import java.time.ZoneId;

public class RequestHeaderHolder {

  protected static final ThreadLocal<RequestHeader> threadLocal = ThreadLocal.withInitial(() -> {
    RequestHeader header = new RequestHeader();
    return header;
  });

  public static String getTimeZone(){
    String serverTimeZone = threadLocal.get().getTimezone();
    if(serverTimeZone == null || "".equalsIgnoreCase(serverTimeZone)){
      serverTimeZone = ZoneId.systemDefault().getId();
    }
    try {
      ZoneId.of(serverTimeZone);
    } catch (Throwable e){
      serverTimeZone = ZoneId.systemDefault().getId();
    }
    return serverTimeZone;
  }
  public static void setTimeZone(String serverTimeZone) {
    threadLocal.get().setTimezone(serverTimeZone);
  }

  public static String getTenantId(){
    RequestHeader heard = threadLocal.get();
    if(heard != null && !heard.getTenantId().isEmpty()){
      return heard.getTenantId();
    } else {
      return "";
    }
  }

  public static String getTenantCode(){
    RequestHeader heard = threadLocal.get();
    if(heard != null && !heard.getTenantCode().isEmpty()){
      return heard.getTenantCode();
    } else {
      return "";
    }
  }

  public static String getTenantName(){
    RequestHeader heard = threadLocal.get();
    if(heard != null && !heard.getTenantName().isEmpty()){
      return heard.getTenantCode();
    } else {
      return "";
    }
  }

  public static void setTenantId(String tenantId){
    RequestHeader heard = threadLocal.get();
    heard.setTenantId(tenantId);
  }

  public static void setTenantCode(String tenantCode){
    RequestHeader heard = threadLocal.get();
    heard.setTenantCode(tenantCode);
  }

  public static void setTenantName(String tenantName){
    RequestHeader heard = threadLocal.get();
    heard.setTenantName(tenantName);
  }
}
