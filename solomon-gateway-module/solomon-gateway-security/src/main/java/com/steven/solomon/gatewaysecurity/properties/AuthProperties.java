package com.steven.solomon.gatewaysecurity.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 多租户鉴权配置。 */
@ConfigurationProperties("gateway.security")
public class AuthProperties {

  /** 是否启用接口鉴权。 */
  private boolean enabled = true;

  /** JWT 签发方。 */
  private String issuer = "gateway-security";

  /** JWT 密钥，必须由业务系统显式配置。 */
  private String secret;

  /** Token 有效期，单位为秒。 */
  private long expireSeconds = 7200;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getExpireSeconds() {
    return expireSeconds;
  }

  public void setExpireSeconds(long expireSeconds) {
    this.expireSeconds = expireSeconds;
  }
}
