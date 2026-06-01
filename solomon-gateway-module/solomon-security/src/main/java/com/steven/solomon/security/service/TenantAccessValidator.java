package com.steven.solomon.security.service;

import com.steven.solomon.security.model.SecurityUser;

/**
 * 租户访问校验扩展点。
 *
 * <p>业务方可以覆盖默认 Bean，接入租户状态、用户归属关系或套餐有效期校验。</p>
 */
@FunctionalInterface
public interface TenantAccessValidator {

  /**
   * 判断当前用户是否允许访问 Token 指定租户。
   */
  boolean validate(SecurityUser user);
}
