package com.steven.solomon.security.utils;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户快捷访问工具。
 */
public final class SecurityContextUtils {

  private SecurityContextUtils() {
  }

  /**
   * 获取当前登录用户，未登录时返回 null。
   */
  public static SecurityUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.getPrincipal() instanceof SecurityUser user
        ? user
        : null;
  }

  /**
   * 获取当前租户编码，未登录时返回空字符串。
   */
  public static String getTenantCode() {
    SecurityUser user = getCurrentUser();
    return user == null ? StrUtil.EMPTY : user.tenantCode();
  }
}
