package com.steven.solomon.holder;

import java.time.ZoneId;

public class RequestHeaderHolder {

  protected static final ThreadLocal<RequestHeader> THREAD_LOCAL = ThreadLocal.withInitial(RequestHeader::new);

  /**
   * 获取当前时区
   */
  public static String getTimeZone() {
    String serverTimeZone = THREAD_LOCAL.get().getTimezone();
    if (serverTimeZone.isEmpty()) {
      serverTimeZone = ZoneId.systemDefault().getId();
    }
    try {
      ZoneId.of(serverTimeZone);
    } catch (Exception e) {
      serverTimeZone = ZoneId.systemDefault().getId();
    }
    return serverTimeZone;
  }

  public static void setTimeZone(String serverTimeZone) {
    THREAD_LOCAL.get().setTimezone(serverTimeZone);
  }

  /**
   * 获取租户ID
   */
  public static String getTenantId() {
    RequestHeader header = THREAD_LOCAL.get();
    if (header != null && !header.getTenantId().isEmpty()) {
      return header.getTenantId();
    }
    return "";
  }

  /**
   * 获取租户编码
   */
  public static String getTenantCode() {
    RequestHeader header = THREAD_LOCAL.get();
    if (header != null && !header.getTenantCode().isEmpty()) {
      return header.getTenantCode();
    }
    return "";
  }

  /**
   * 获取租户名称
   */
  public static String getTenantName() {
    RequestHeader header = THREAD_LOCAL.get();
    if (header != null && !header.getTenantName().isEmpty()) {
      return header.getTenantName();
    }
    return "";
  }

  public static void setTenantId(String tenantId) {
    THREAD_LOCAL.get().setTenantId(tenantId);
  }

  public static void setTenantCode(String tenantCode) {
    THREAD_LOCAL.get().setTenantCode(tenantCode);
  }

  public static void setTenantName(String tenantName) {
    THREAD_LOCAL.get().setTenantName(tenantName);
  }

  /**
   * 清理ThreadLocal，防止内存泄漏
   * <p>⚠️ 重要：在请求处理完成后必须调用此方法</p>
   */
  public static void remove() {
    THREAD_LOCAL.remove();
  }
}
