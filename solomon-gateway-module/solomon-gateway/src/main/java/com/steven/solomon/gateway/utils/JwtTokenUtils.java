package com.steven.solomon.gateway.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 生成与解析工具。
 *
 * <p>统一使用 Hutool JWT，Token 中预留租户编码字段，后续可直接用于多租户切换。</p>
 */
public class JwtTokenUtils {

  public static final String TENANT_CODE = "tenantCode";

  private final GatewayJwtProperties properties;
  private final JWTSigner signer;

  public JwtTokenUtils(GatewayJwtProperties properties) {
    if (ObjectUtil.isEmpty(properties) || StrUtil.isBlank(properties.getSecret())) {
      throw new IllegalArgumentException("gateway.jwt.secret 不能为空");
    }
    this.properties = properties;
    this.signer = JWTSignerUtil.hs256(properties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 生成包含租户编码的 Token。
   */
  public String generateToken(GatewayTokenClaims tokenClaims) {
    if (ObjectUtil.isEmpty(tokenClaims) || StrUtil.isBlank(tokenClaims.subject())) {
      throw new IllegalArgumentException("Token 用户标识不能为空");
    }
    Date now = new Date();
    JWT jwt = JWT.create();
    if (ObjectUtil.isNotEmpty(tokenClaims.claims())) {
      jwt.addPayloads(tokenClaims.claims());
    }
    return jwt
        .setIssuer(properties.getIssuer())
        .setSubject(tokenClaims.subject())
        .setJWTId(IdUtil.fastSimpleUUID())
        .setIssuedAt(now)
        .setExpiresAt(new Date(now.getTime() + properties.getExpireSeconds() * 1000))
        .setPayload(TENANT_CODE, tokenClaims.tenantCode())
        .sign(signer);
  }

  /**
   * 校验签名和有效期。
   */
  public boolean validateToken(String token) {
    String rawToken = removeBearerPrefix(token);
    if (StrUtil.isBlank(rawToken)) {
      return false;
    }
    try {
      JWT jwt = JWT.of(rawToken);
      JWTValidator.of(jwt).validateAlgorithm(signer).validateDate();
      return StrUtil.equals(properties.getIssuer(), getStringPayload(jwt, RegisteredPayload.ISSUER));
    } catch (RuntimeException exception) {
      return false;
    }
  }

  /**
   * 解析 Token，签名或有效期无效时抛出异常。
   */
  public JWT parseToken(String token) {
    String rawToken = removeBearerPrefix(token);
    if (!validateToken(rawToken)) {
      throw new IllegalArgumentException("Token 无效或已过期");
    }
    return JWT.of(rawToken);
  }

  /**
   * 从 Token 中获取租户编码。
   */
  public String getTenantCode(String token) {
    return getStringPayload(parseToken(token), TENANT_CODE);
  }

  /**
   * 从 Token 中获取用户唯一标识。
   */
  public String getSubject(String token) {
    return getStringPayload(parseToken(token), RegisteredPayload.SUBJECT);
  }

  /**
   * 获取 Token 中的全部载荷。
   */
  public Map<String, Object> getPayloads(String token) {
    return Map.copyOf(parseToken(token).getPayloads());
  }

  /**
   * 移除 Authorization 中的 Bearer 前缀。
   */
  public String removeBearerPrefix(String token) {
    if (StrUtil.isBlank(token)) {
      return StrUtil.EMPTY;
    }
    String trimmedToken = StrUtil.trim(token);
    return StrUtil.startWithIgnoreCase(trimmedToken, GatewayHeaders.BEARER_PREFIX)
        ? StrUtil.trim(StrUtil.subAfter(trimmedToken, GatewayHeaders.BEARER_PREFIX, false))
        : trimmedToken;
  }

  private String getStringPayload(JWT jwt, String name) {
    Object payload = jwt.getPayload(name);
    return ObjectUtil.isEmpty(payload) ? StrUtil.EMPTY : payload.toString();
  }
}
