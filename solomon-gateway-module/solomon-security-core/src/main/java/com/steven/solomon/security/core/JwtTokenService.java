package com.steven.solomon.security.core;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;

public class JwtTokenService {

    private static final Logger logger = LoggerUtils.logger(JwtTokenService.class);

    private static final int MIN_SECRET_BYTES = 32;
    private static final int MAX_TOKEN_LENGTH = 8192;
    private static final int MAX_HEADER_VALUE_LENGTH = 128;
    private static final Pattern IDENTITY_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:@-]{0,127}");
    private static final String TENANT_CODE_KEY = "tenantCode";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KEY_ID_HEADER = "kid";
    private static final String TOKEN_ID_KEY = "jti";
    private static final String TOKEN_TYPE_KEY = "typ";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtTokenProperties properties;
    private final TenantModeResolver tenantModeResolver;
    private final TokenRevocationStore revocationStore;
    private final String activeKeyId;
    private final Map<String, JWTSigner> signers;

    public JwtTokenService(JwtTokenProperties properties) {
        this(properties, new TenantModeResolver(new TenantModeProperties()), new InMemoryTokenRevocationStore());
    }

    public JwtTokenService(JwtTokenProperties properties, TenantModeResolver tenantModeResolver) {
        this(properties, tenantModeResolver, new InMemoryTokenRevocationStore());
    }

    public JwtTokenService(
            JwtTokenProperties properties,
            TenantModeResolver tenantModeResolver,
            TokenRevocationStore revocationStore) {
        if (properties == null) {
            throw new IllegalArgumentException("security.jwt must not be null");
        }
        if (StrUtil.isBlank(properties.getIssuer())) {
            throw new IllegalArgumentException("security.jwt.issuer must not be blank");
        }
        if (properties.getExpireSeconds() <= 0) {
            throw new IllegalArgumentException("security.jwt.expire-seconds must be greater than 0");
        }
        if (properties.getRefreshExpireSeconds() <= 0) {
            throw new IllegalArgumentException("security.jwt.refresh-expire-seconds must be greater than 0");
        }
        if (properties.getClockSkewSeconds() < 0) {
            throw new IllegalArgumentException("security.jwt.clock-skew-seconds must not be negative");
        }
        this.properties = properties;
        this.tenantModeResolver = tenantModeResolver;
        this.revocationStore = revocationStore == null ? new InMemoryTokenRevocationStore() : revocationStore;
        this.activeKeyId = resolveActiveKeyId(properties);
        this.signers = buildSigners(properties, activeKeyId);
        logger.info("JWT token service initialized, issuer={}, expireSeconds={}, refreshExpireSeconds={}, activeKeyId={}",
                properties.getIssuer(), properties.getExpireSeconds(), properties.getRefreshExpireSeconds(), activeKeyId);
    }

    public String createToken(TokenClaims claims) {
        return createToken(claims, ACCESS_TOKEN_TYPE, properties.getExpireSeconds());
    }

    public String createRefreshToken(TokenClaims claims) {
        return createToken(claims, REFRESH_TOKEN_TYPE, properties.getRefreshExpireSeconds());
    }

    public TokenPair createTokenPair(TokenClaims claims) {
        return new TokenPair(createToken(claims), createRefreshToken(claims));
    }

    public TokenPair refreshToken(String refreshToken) {
        ParsedToken parsed = parse(refreshToken, REFRESH_TOKEN_TYPE);
        if (parsed == null) {
            return null;
        }
        revokeParsedToken(parsed);
        return createTokenPair(parsed.claims());
    }

    public TokenClaims parseToken(String token) {
        ParsedToken parsed = parse(token, ACCESS_TOKEN_TYPE);
        return parsed == null ? null : parsed.claims();
    }

    public TokenClaims parseRefreshToken(String token) {
        ParsedToken parsed = parse(token, REFRESH_TOKEN_TYPE);
        return parsed == null ? null : parsed.claims();
    }

    public boolean revokeToken(String token) {
        ParsedToken parsed = parse(token, null);
        if (parsed == null) {
            return false;
        }
        revokeParsedToken(parsed);
        return true;
    }

    public String resolveBearerToken(String authorization) {
        if (StrUtil.isBlank(authorization)
                || !StrUtil.startWithIgnoreCase(authorization, BEARER_PREFIX)) {
            return null;
        }
        return StrUtil.trim(authorization.substring(BEARER_PREFIX.length()));
    }

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

    public boolean isSafeIdentity(String value) {
        return isSafeHeaderValue(value) && IDENTITY_PATTERN.matcher(StrUtil.trim(value)).matches();
    }

    private String createToken(TokenClaims claims, String tokenType, long expireSeconds) {
        String tenantCode = claims == null ? null : tenantModeResolver.resolve(claims.tenantCode());
        if (claims == null || !isSafeIdentity(claims.userId()) || !isSafeIdentity(tenantCode)) {
            throw new IllegalArgumentException("userId or tenantCode format is invalid");
        }
        long now = System.currentTimeMillis();
        long expiresAt = now + expireSeconds * 1000L;
        return JWT.create()
                .setIssuer(properties.getIssuer())
                .setSubject(StrUtil.trim(claims.userId()))
                .setIssuedAt(new Date(now))
                .setExpiresAt(new Date(expiresAt))
                .setPayload(TENANT_CODE_KEY, StrUtil.trim(tenantCode))
                .setPayload(TOKEN_ID_KEY, UUID.randomUUID().toString())
                .setPayload(TOKEN_TYPE_KEY, tokenType)
                .setHeader(KEY_ID_HEADER, activeKeyId)
                .sign(currentSigner());
    }

    private ParsedToken parse(String token, String expectedType) {
        if (StrUtil.isBlank(token) || token.length() > MAX_TOKEN_LENGTH) {
            return null;
        }
        try {
            JWT jwt = JWT.of(token);
            JWTSigner tokenSigner = resolveSigner(jwt);
            if (tokenSigner == null) {
                logger.warn("JWT key id is unknown");
                return null;
            }
            jwt.setSigner(tokenSigner);
            if (!jwt.verify() || !jwt.validate(properties.getClockSkewSeconds())) {
                logger.warn("JWT verification failed");
                return null;
            }
            if (!isExpectedIssuer(jwt)) {
                return null;
            }
            String tokenType = stringPayload(jwt, TOKEN_TYPE_KEY);
            if (expectedType != null && StrUtil.isNotBlank(tokenType) && !expectedType.equals(tokenType)) {
                logger.warn("JWT token type mismatch, expected={}, actual={}", expectedType, tokenType);
                return null;
            }
            if (expectedType != null && StrUtil.isBlank(tokenType) && !ACCESS_TOKEN_TYPE.equals(expectedType)) {
                logger.warn("JWT token type is missing");
                return null;
            }
            String tokenId = stringPayload(jwt, TOKEN_ID_KEY);
            if (StrUtil.isNotBlank(tokenId) && revocationStore.isRevoked(tokenId)) {
                logger.warn("JWT has been revoked");
                return null;
            }
            TokenClaims claims = parseClaims(jwt);
            if (claims == null) {
                return null;
            }
            return new ParsedToken(claims, tokenId, expiresAtMillis(jwt), tokenType);
        } catch (RuntimeException ex) {
            logger.warn("JWT parse failed: {}", ex.getMessage());
            return null;
        }
    }

    private boolean isExpectedIssuer(JWT jwt) {
        Object issuer = jwt.getPayload(RegisteredPayload.ISSUER);
        if (issuer == null || !StrUtil.equals(properties.getIssuer(), issuer.toString())) {
            logger.warn("JWT issuer mismatch, expected={}", properties.getIssuer());
            return false;
        }
        return true;
    }

    private TokenClaims parseClaims(JWT jwt) {
        Object subject = jwt.getPayload(RegisteredPayload.SUBJECT);
        Object tenantPayload = jwt.getPayload(TENANT_CODE_KEY);
        String userId = subject == null ? null : subject.toString();
        String tenantCode = tenantModeResolver.resolve(tenantPayload == null ? null : tenantPayload.toString());
        if (!isSafeIdentity(userId) || !isSafeIdentity(tenantCode)) {
            logger.warn("JWT contains invalid identity, userId={}", userId);
            return null;
        }
        return new TokenClaims(StrUtil.trim(userId), StrUtil.trim(tenantCode));
    }

    private void revokeParsedToken(ParsedToken parsed) {
        if (parsed == null || StrUtil.isBlank(parsed.tokenId())) {
            return;
        }
        revocationStore.revoke(parsed.tokenId(), parsed.expiresAtMillis());
    }

    private long expiresAtMillis(JWT jwt) {
        Object value = jwt.getPayload(RegisteredPayload.EXPIRES_AT);
        if (value instanceof Date date) {
            return date.getTime();
        }
        if (value instanceof Number number) {
            long raw = number.longValue();
            return raw < 10_000_000_000L ? raw * 1000L : raw;
        }
        return System.currentTimeMillis() + properties.getClockSkewSeconds() * 1000L;
    }

    private String stringPayload(JWT jwt, String key) {
        Object value = jwt.getPayload(key);
        return value == null ? null : value.toString();
    }

    private String resolveActiveKeyId(JwtTokenProperties properties) {
        return StrUtil.blankToDefault(properties.getKeyId(), "default");
    }

    private Map<String, JWTSigner> buildSigners(JwtTokenProperties properties, String activeKeyId) {
        Map<String, String> secrets = new LinkedHashMap<>();
        if (properties.getSecrets() != null) {
            secrets.putAll(properties.getSecrets());
        }
        if (StrUtil.isNotBlank(properties.getSecret())) {
            secrets.put(activeKeyId, properties.getSecret());
        }
        if (secrets.isEmpty()) {
            throw new IllegalArgumentException(
                    "security.jwt.secret or security.jwt.secrets must be configured");
        }
        if (!secrets.containsKey(activeKeyId)) {
            throw new IllegalArgumentException("security.jwt.key-id must exist in security.jwt.secrets");
        }
        Map<String, JWTSigner> result = new LinkedHashMap<>();
        secrets.forEach((keyId, secret) -> result.put(validateKeyId(keyId), createSigner(secret)));
        return Map.copyOf(result);
    }

    private String validateKeyId(String keyId) {
        if (!isSafeIdentity(keyId)) {
            throw new IllegalArgumentException("security.jwt key id format is invalid");
        }
        return StrUtil.trim(keyId);
    }

    private JWTSigner createSigner(String secret) {
        if (StrUtil.isBlank(secret)) {
            throw new IllegalArgumentException("security.jwt secret must not be blank");
        }
        if (secret.getBytes(CharsetUtil.CHARSET_UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("security.jwt secret must be at least 32 bytes");
        }
        return JWTSignerUtil.hs256(secret.getBytes(CharsetUtil.CHARSET_UTF_8));
    }

    private JWTSigner currentSigner() {
        return signers.get(activeKeyId);
    }

    private JWTSigner resolveSigner(JWT jwt) {
        Object keyId = jwt.getHeader(KEY_ID_HEADER);
        if (keyId == null || StrUtil.isBlank(keyId.toString())) {
            return currentSigner();
        }
        return signers.get(keyId.toString());
    }

    private record ParsedToken(
            TokenClaims claims,
            String tokenId,
            long expiresAtMillis,
            String tokenType) {
    }
}
