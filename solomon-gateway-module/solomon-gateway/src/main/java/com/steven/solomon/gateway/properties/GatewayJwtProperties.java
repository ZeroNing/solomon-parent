package com.steven.solomon.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关 JWT 配置。
 */
@ConfigurationProperties("gateway.jwt")
public class GatewayJwtProperties {

  /**
   * JWT 签名密钥，生产环境必须覆盖默认值。
   */
  private String secret;

  /**
   * Token 有效期，单位秒。
   */
  private long expireSeconds = 7200;

  /**
   * Token 签发者。
   */
  private String issuer = "gateway";

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

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }
}
