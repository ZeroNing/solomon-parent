package com.steven.solomon.gateway.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import java.util.Date;
import java.util.regex.Pattern;

/** JWT 生成、提取与校验工具。 */
public class JwtTokenUtils {

  private static final int MIN_SECRET_BYTES = 32;
  private static final int MAX_TOKEN_LENGTH = 8192;
  private static final int MAX_HEADER_VALUE_LENGTH = 128;
  private static final Pattern IDENTITY_PATTERN =
      Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:@-]{0,127}");
  private static final String TENANT_CODE = "tenantCode";

  private final GatewayJwtProperties properties;
  private final TenantModeResolver tenantModeResolver;
  private final JWTSigner signer;

  public JwtTokenUtils(GatewayJwtProperties properties) {
    this(properties, new TenantModeResolver(new TenantModeProperties()));
  }

  public JwtTokenUtils(GatewayJwtProperties properties, TenantModeResolver tenantModeResolver) {
    if (properties == null || StrUtil.isBlank(properties.getSecret())) {
      throw new IllegalArgumentException("gateway.jwt.secret 不能为空");
    }
    if (properties.getSecret().getBytes(CharsetUtil.CHARSET_UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalArgumentException("gateway.jwt.secret 长度不能小于 32 字节");
    }
    if (StrUtil.isBlank(properties.getIssuer())) {
      throw new IllegalArgumentException("gateway.jwt.issuer 不能为空");
    }
    if (properties.getExpireSeconds() <= 0) {
      throw new IllegalArgumentException("gateway.jwt.expire-seconds 必须大于 0");
    }
    this.properties = properties;
    this.tenantModeResolver = tenantModeResolver;
    this.signer = JWTSignerUtil.hs256(properties.getSecret().getBytes(CharsetUtil.CHARSET_UTF_8));
  }

  /** 生成携带租户、角色和权限的 Token。 */
  public String createToken(GatewayTokenClaims claims) {
    String tenantCode = claims == null ? null : tenantModeResolver.resolve(claims.tenantCode());
    if (claims == null || !isSafeIdentity(claims.userId()) || !isSafeIdentity(tenantCode)) {
      throw new IllegalArgumentException("用户和租户编码格式不正确");
    }
    long now = System.currentTimeMillis();
    long expiresAt = Math.addExact(now, Math.multiplyExact(properties.getExpireSeconds(), 1000));
    return JWT.create()
        .setIssuer(properties.getIssuer())
        .setSubject(StrUtil.trim(claims.userId()))
        .setIssuedAt(new Date(now))
        .setExpiresAt(new Date(expiresAt))
        .setPayload(TENANT_CODE, StrUtil.trim(tenantCode))
        .setSigner(signer)
        .sign();
  }

  /** 从 Authorization 请求头提取 Bearer Token。 */
  public String resolveBearerToken(String authorization) {
    if (StrUtil.isBlank(authorization)
        || !StrUtil.startWithIgnoreCase(authorization, GatewayHeaders.BEARER_PREFIX)) {
      return null;
    }
    return StrUtil.trim(authorization.substring(GatewayHeaders.BEARER_PREFIX.length()));
  }

  /** 校验签名、签发方和过期时间，失败时返回 null。 */
  public GatewayTokenClaims parseToken(String token) {
    if (StrUtil.isBlank(token) || token.length() > MAX_TOKEN_LENGTH) {
      return null;
    }
    try {
      JWT jwt = JWT.of(token).setSigner(signer);
      if (!jwt.verify() || !jwt.validate(0)
          || !StrUtil.equals(properties.getIssuer(),
              Convert.toStr(jwt.getPayload(RegisteredPayload.ISSUER)))) {
        return null;
      }
      String userId = Convert.toStr(jwt.getPayload(RegisteredPayload.SUBJECT));
      String tenantCode = tenantModeResolver.resolve(Convert.toStr(jwt.getPayload(TENANT_CODE)));
      if (!isSafeIdentity(userId) || !isSafeIdentity(tenantCode)) {
        return null;
      }
      return new GatewayTokenClaims(StrUtil.trim(userId), StrUtil.trim(tenantCode));
    } catch (RuntimeException ex) {
      return null;
    }
  }

  /** 校验即将透传到下游的值，避免超长或控制字符污染请求头。 */
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

  /** 校验用户和租户标识，限制为可安全透传并适合作为资源路由键的字符。 */
  public boolean isSafeIdentity(String value) {
    return isSafeHeaderValue(value) && IDENTITY_PATTERN.matcher(StrUtil.trim(value)).matches();
  }
}
