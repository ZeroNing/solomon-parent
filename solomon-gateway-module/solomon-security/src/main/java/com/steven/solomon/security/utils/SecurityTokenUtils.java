package com.steven.solomon.security.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.steven.solomon.security.constant.SecurityHeaders;
import com.steven.solomon.security.model.SecurityTokenClaims;
import com.steven.solomon.security.model.SecurityUser;
import com.steven.solomon.security.properties.SecurityJwtProperties;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 独立 JWT 工具，不依赖网关模块。
 */
public class SecurityTokenUtils {

  public static final String TENANT_CODE = "tenantCode";
  public static final String TENANT_ID = "tenantId";
  public static final String TENANT_NAME = "tenantName";
  public static final String AUTHORITIES = "authorities";

  private final SecurityJwtProperties properties;
  private final JWTSigner signer;

  public SecurityTokenUtils(SecurityJwtProperties properties) {
    if (ObjectUtil.isEmpty(properties) || StrUtil.isBlank(properties.getSecret())) {
      throw new IllegalArgumentException("security.jwt.secret 不能为空");
    }
    this.properties = properties;
    this.signer = JWTSignerUtil.hs256(properties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 生成包含租户和权限信息的 Token。
   */
  public String generateToken(SecurityTokenClaims tokenClaims) {
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
        .setPayload(TENANT_ID, tokenClaims.tenantId())
        .setPayload(TENANT_NAME, tokenClaims.tenantName())
        .setPayload(AUTHORITIES, ObjectUtil.defaultIfNull(tokenClaims.authorities(), Set.of()))
        .sign(signer);
  }

  /**
   * 校验签名、有效期和签发者。
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
   * 解析 Token，Token 无效时抛出异常。
   */
  public JWT parseToken(String token) {
    String rawToken = removeBearerPrefix(token);
    if (!validateToken(rawToken)) {
      throw new IllegalArgumentException("Token 无效或已过期");
    }
    return JWT.of(rawToken);
  }

  /**
   * 将 Token 转换为 Spring Security 登录用户。
   */
  public SecurityUser parseUser(String token) {
    JWT jwt = parseToken(token);
    String subject = getStringPayload(jwt, RegisteredPayload.SUBJECT);
    if (StrUtil.isBlank(subject)) {
      throw new IllegalArgumentException("Token 缺少用户标识");
    }
    return SecurityUser.of(
        subject,
        getStringPayload(jwt, TENANT_CODE),
        getStringPayload(jwt, TENANT_ID),
        getStringPayload(jwt, TENANT_NAME),
        Convert.toList(String.class, jwt.getPayload(AUTHORITIES)));
  }

  /**
   * 获取 Token 中的全部载荷。
   */
  public Map<String, Object> getPayloads(String token) {
    return Collections.unmodifiableMap(new LinkedHashMap<>(parseToken(token).getPayloads()));
  }

  /**
   * 移除 Authorization 中的 Bearer 前缀。
   */
  public String removeBearerPrefix(String token) {
    if (StrUtil.isBlank(token)) {
      return StrUtil.EMPTY;
    }
    String trimmedToken = StrUtil.trim(token);
    return StrUtil.startWithIgnoreCase(trimmedToken, SecurityHeaders.BEARER_PREFIX)
        ? StrUtil.trim(StrUtil.subAfter(trimmedToken, SecurityHeaders.BEARER_PREFIX, false))
        : trimmedToken;
  }

  private String getStringPayload(JWT jwt, String name) {
    Object payload = jwt.getPayload(name);
    return ObjectUtil.isEmpty(payload) ? StrUtil.EMPTY : payload.toString();
  }
}
