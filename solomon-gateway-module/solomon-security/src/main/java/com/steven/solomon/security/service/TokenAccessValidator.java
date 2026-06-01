package com.steven.solomon.security.service;

import com.steven.solomon.security.model.SecurityUser;

/**
 * Token 状态校验扩展点。
 *
 * <p>业务方可以覆盖默认 Bean，接入 Redis 黑名单、用户冻结或强制下线逻辑。</p>
 */
@FunctionalInterface
public interface TokenAccessValidator {

  /**
   * 判断 Token 当前是否仍然允许访问。
   */
  boolean validate(String token, SecurityUser user);
}
