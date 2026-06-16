package com.steven.solomon.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 令牌配置属性。
 *
 * <p>配置前缀 {@code gateway.jwt}，控制令牌的签名密钥、签发方和过期时间。
 * 密钥长度必须不少于 32 字节，否则构造 {@code JwtTokenService} 时抛出
 * {@link IllegalArgumentException}，避免使用弱密钥。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "gateway.jwt")
public class GatewayJwtProperties {

    /** HMAC 签名密钥，至少 32 字节。 */
    private String secret = "solomon-gateway-default-secret-key-32bytes";

    /** 令牌签发方，校验令牌时必须匹配。 */
    private String issuer = "gateway";

    /** 令牌有效期（秒），默认 2 小时。 */
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
