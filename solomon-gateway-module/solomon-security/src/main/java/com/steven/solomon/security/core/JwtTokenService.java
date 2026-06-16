package com.steven.solomon.security.core;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;

import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.security.properties.SecurityJwtProperties;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.Date;
import java.util.regex.Pattern;
import org.slf4j.Logger;

/**
 * JWT 令牌服务，负责签发与解密令牌。
 *
 * <p>基于 Hutool JWT 实现 HS256 签名，校验签名、签发方和过期时间。
 * 仅保存用户标识和租户编码两个声明，不承载角色/权限等敏感信息（这些由客户的
 * {@code AccessValidator} 实时查询），避免令牌过期前权限变更无法生效。</p>
 *
 * <p>安全约束：</p>
 * <ul>
 *   <li>密钥不少于 32 字节，防止弱密钥被暴力破解。</li>
 *   <li>用户/租户标识只允许字母、数字及 {@code . _ : @ -}，最大 128 字符。</li>
 *   <li>单租户模式下租户编码为空时自动回退默认租户。</li>
 * </ul>
 *
 * @author steven
 */
public class JwtTokenService {

    private static final Logger logger = LoggerUtils.logger(JwtTokenService.class);

    private static final int MIN_SECRET_BYTES = 32;
    private static final int MAX_TOKEN_LENGTH = 8192;
    private static final int MAX_HEADER_VALUE_LENGTH = 128;
    private static final Pattern IDENTITY_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:@-]{0,127}");
    private static final String TENANT_CODE_KEY = "tenantCode";
    /** Bearer Token 前缀。 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecurityJwtProperties properties;
    private final TenantModeResolver tenantModeResolver;
    private final JWTSigner signer;

    /**
     * 使用默认单租户解析器构造。
     *
     * @param properties JWT 配置，密钥不可少于 32 字节
     * @throws IllegalArgumentException 密钥为空或过短、签发方为空、过期时间非正
     */
    public JwtTokenService(SecurityJwtProperties properties) {
        this(properties, new TenantModeResolver(new TenantModeProperties()));
    }

    /**
     * 指定租户模式解析器构造，用于多租户部署。
     */
    public JwtTokenService(SecurityJwtProperties properties, TenantModeResolver tenantModeResolver) {
        if (properties == null || StrUtil.isBlank(properties.getSecret())) {
            throw new IllegalArgumentException("security.jwt.secret 不能为空");
        }
        if (properties.getSecret().getBytes(CharsetUtil.CHARSET_UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("security.jwt.secret 长度不能小于 32 字节");
        }
        if (StrUtil.isBlank(properties.getIssuer())) {
            throw new IllegalArgumentException("security.jwt.issuer 不能为空");
        }
        if (properties.getExpireSeconds() <= 0) {
            throw new IllegalArgumentException("security.jwt.expire-seconds 必须大于 0");
        }
        this.properties = properties;
        this.tenantModeResolver = tenantModeResolver;
        this.signer = JWTSignerUtil.hs256(properties.getSecret().getBytes(CharsetUtil.CHARSET_UTF_8));
        logger.info("JWT 令牌服务初始化完成, 签发方={}, 有效期={}秒", properties.getIssuer(), properties.getExpireSeconds());
    }

    /**
     * 签发携带用户标识和租户编码的令牌。
     *
     * @param claims 令牌声明
     * @return 已签名的 JWT 字符串
     * @throws IllegalArgumentException 用户或租户标识格式非法
     */
    public String createToken(TokenClaims claims) {
        String tenantCode = claims == null ? null : tenantModeResolver.resolve(claims.tenantCode());
        if (claims == null || !isSafeIdentity(claims.userId()) || !isSafeIdentity(tenantCode)) {
            throw new IllegalArgumentException("用户标识或租户编码格式不正确");
        }
        long now = System.currentTimeMillis();
        long expiresAt = now + properties.getExpireSeconds() * 1000L;
        return JWT.create()
                .setIssuer(properties.getIssuer())
                .setSubject(StrUtil.trim(claims.userId()))
                .setIssuedAt(new Date(now))
                .setExpiresAt(new Date(expiresAt))
                .setPayload(TENANT_CODE_KEY, StrUtil.trim(tenantCode))
                .setSigner(signer)
                .sign();
    }

    /**
     * 从 Authorization 头提取 Bearer Token。
     *
     * @param authorization Authorization 头值
     * @return Token 字符串；非 Bearer 格式或为空时返回 null
     */
    public String resolveBearerToken(String authorization) {
        if (StrUtil.isBlank(authorization)
                || !StrUtil.startWithIgnoreCase(authorization, BEARER_PREFIX)) {
            return null;
        }
        return StrUtil.trim(authorization.substring(BEARER_PREFIX.length()));
    }

    /**
     * 解密并校验令牌。
     *
     * @param token JWT 字符串
     * @return 解析出的令牌声明；令牌无效/过期/签名不符时返回 null
     */
    public TokenClaims parseToken(String token) {
        if (StrUtil.isBlank(token) || token.length() > MAX_TOKEN_LENGTH) {
            return null;
        }
        try {
            JWT jwt = JWT.of(token).setSigner(signer);
            if (!jwt.verify() || !jwt.validate(0)) {
                logger.warn("令牌校验失败: 签名或过期时间无效");
                return null;
            }
            String tokenIssuer = jwt.getPayload(RegisteredPayload.ISSUER).toString();
            if (!StrUtil.equals(properties.getIssuer(), tokenIssuer)) {
                logger.warn("令牌签发方不匹配, 期望={}, 实际={}", properties.getIssuer(), tokenIssuer);
                return null;
            }
            String userId = String.valueOf(jwt.getPayload(RegisteredPayload.SUBJECT));
            String tenantCode = tenantModeResolver.resolve(
                    String.valueOf(jwt.getPayload(TENANT_CODE_KEY)));
            if (!isSafeIdentity(userId) || !isSafeIdentity(tenantCode)) {
                logger.warn("令牌声明包含非法标识, userId={}", userId);
                return null;
            }
            return new TokenClaims(StrUtil.trim(userId), StrUtil.trim(tenantCode));
        } catch (RuntimeException ex) {
            logger.warn("令牌解析异常: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * 校验头值是否安全（非空、长度合法、无控制字符）。
     */
    public boolean isSafeHeaderValue(String value) {
        if (StrUtil.isBlank(value) || value.length() > MAX_HEADER_VALUE_LENGTH) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验用户/租户标识是否安全。
     */
    public boolean isSafeIdentity(String value) {
        return isSafeHeaderValue(value) && IDENTITY_PATTERN.matcher(StrUtil.trim(value)).matches();
    }
}
