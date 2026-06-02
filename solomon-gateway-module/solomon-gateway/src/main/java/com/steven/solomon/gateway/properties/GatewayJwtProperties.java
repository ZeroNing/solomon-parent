package com.steven.solomon.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 网关 JWT 配置。 */
@ConfigurationProperties("gateway.jwt")
public class GatewayJwtProperties {

  /** JWT 密钥，必须由引入模块显式配置。 */
  private String secret;

  /** JWT 签发方。 */
  private String issuer = "gateway";

  /** Token 有效期，单位为秒。 */
  private long expireSeconds = 7200;

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public long getExpireSeconds() {
    return expireSeconds;
  }

  public void setExpireSeconds(long expireSeconds) {
    this.expireSeconds = expireSeconds;
  }
}
