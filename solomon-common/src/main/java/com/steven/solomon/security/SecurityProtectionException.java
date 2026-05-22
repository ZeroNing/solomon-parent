package com.steven.solomon.security;

/**
 * 接口安全防护异常。
 *
 * <p>过滤器内部直接把该异常转换为 JSON 响应，避免异常继续进入业务控制器。</p>
 */
public class SecurityProtectionException extends RuntimeException {

  private final int status;

  private final String code;

  public SecurityProtectionException(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public int getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }
}
