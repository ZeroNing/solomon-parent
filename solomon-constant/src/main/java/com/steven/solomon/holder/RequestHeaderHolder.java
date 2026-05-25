package com.steven.solomon.holder;

import java.time.ZoneId;

/**
 * 请求头上下文持有器。
 *
 * <p>该类通过 ThreadLocal 保存当前请求的租户、时区等头信息，方便业务代码在调用链深处读取。
 * Web 请求结束后必须调用 {@link #remove()}，否则线程池复用时会出现上下文串用。</p>
 */
public class RequestHeaderHolder {

  private static final ThreadLocal<RequestHeader> THREAD_LOCAL =
      ThreadLocal.withInitial(RequestHeader::new);

  private RequestHeaderHolder() {
  }

  /**
   * 获取当前请求时区。
   *
   * <p>如果请求头为空或非法，则回退到服务器默认时区，避免下游时间转换抛异常。</p>
   */
  public static String getTimeZone() {
    String timeZone = THREAD_LOCAL.get().getTimezone();
    if (timeZone == null || timeZone.isBlank()) {
      return ZoneId.systemDefault().getId();
    }
    try {
      return ZoneId.of(timeZone).getId();
    } catch (Exception ex) {
      return ZoneId.systemDefault().getId();
    }
  }

  public static void setTimeZone(String timeZone) {
    THREAD_LOCAL.get().setTimezone(timeZone);
  }

  /**
   * 获取当前租户 ID。
   */
  public static String getTenantId() {
    return defaultString(THREAD_LOCAL.get().getTenantId());
  }

  /**
   * 获取当前租户编码。
   */
  public static String getTenantCode() {
    return defaultString(THREAD_LOCAL.get().getTenantCode());
  }

  /**
   * 获取当前租户名称。
   */
  public static String getTenantName() {
    return defaultString(THREAD_LOCAL.get().getTenantName());
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
   * 清理当前线程上下文。
   */
  public static void remove() {
    THREAD_LOCAL.remove();
  }

  private static String defaultString(String value) {
    return value == null || value.isEmpty() ? "" : value;
  }
}
