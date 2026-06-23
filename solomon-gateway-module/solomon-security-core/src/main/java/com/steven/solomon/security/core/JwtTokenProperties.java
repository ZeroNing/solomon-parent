package com.steven.solomon.security.core;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 令牌配置属性。
 *
 * <p>配置前缀 {@code security.jwt}，控制令牌的签名密钥、签发方和过期时间。
 * 密钥长度必须不少于 32 字节，否则构造 {@link JwtTokenService} 时抛出
 * {@link IllegalArgumentException}，避免使用弱密钥。</p>
 *
 * <p>网关层和服务层共用此配置，保证两端的密钥、签发方一致。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "security.jwt")
@Validated
public class JwtTokenProperties {

    /** HMAC 签名密钥，至少 32 字节。 */
    private String secret;

    @NotBlank(message = "security.jwt.key-id must not be blank")
    private String keyId = "default";

    private Map<@NotBlank(message = "security.jwt.secrets key must not be blank") String,
        @NotBlank(message = "security.jwt.secrets value must not be blank") String> secrets = new LinkedHashMap<>();

    /** 令牌签发方，校验令牌时必须匹配。 */
    @NotBlank(message = "security.jwt.issuer must not be blank")
    private String issuer = "solomon";

    /** 令牌有效期（秒），默认 2 小时。 */
    @Min(value = 60, message = "security.jwt.expire-seconds must be at least 60 seconds")
    private long expireSeconds = 7200;

    @Min(value = 60, message = "security.jwt.refresh-expire-seconds must be at least 60 seconds")
    private long refreshExpireSeconds = 2592000;

    @Min(value = 0, message = "security.jwt.clock-skew-seconds must not be negative")
    private long clockSkewSeconds = 60;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public Map<String, String> getSecrets() { return secrets; }
    public void setSecrets(Map<String, String> secrets) { this.secrets = secrets; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public long getExpireSeconds() { return expireSeconds; }
    public void setExpireSeconds(long expireSeconds) { this.expireSeconds = expireSeconds; }
    public long getRefreshExpireSeconds() { return refreshExpireSeconds; }
    public void setRefreshExpireSeconds(long refreshExpireSeconds) { this.refreshExpireSeconds = refreshExpireSeconds; }
    public long getClockSkewSeconds() { return clockSkewSeconds; }
    public void setClockSkewSeconds(long clockSkewSeconds) { this.clockSkewSeconds = clockSkewSeconds; }
}
