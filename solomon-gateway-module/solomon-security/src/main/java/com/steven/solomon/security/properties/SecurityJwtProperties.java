package com.steven.solomon.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 令牌配置属性。
 *
 * <p>配置前缀 {@code security.jwt}，控制令牌的签名密钥、签发方和过期时间。
 * 密钥长度必须不少于 32 字节。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "security.jwt")
public class SecurityJwtProperties {

    /** HMAC 签名密钥，至少 32 字节。 */
    private String secret = "solomon-security-default-secret-32b";

    /** 令牌签发方，校验令牌时必须匹配。 */
    private String issuer = "solomon";

    /** 令牌有效期（秒），默认 2 小时。 */
    private long expireSeconds = 7200;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public long getExpireSeconds() { return expireSeconds; }
    public void setExpireSeconds(long expireSeconds) { this.expireSeconds = expireSeconds; }
}
