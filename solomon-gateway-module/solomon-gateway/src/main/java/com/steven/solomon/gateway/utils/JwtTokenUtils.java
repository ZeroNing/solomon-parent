package com.steven.solomon.gateway.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.model.GatewayTokenClaims;
import com.steven.solomon.gateway.properties.GatewayJwtProperties;
import java.util.Date;

/** JWT 生成、提取与校验工具。 */
public class JwtTokenUtils {

  private static final String TENANT_CODE = "tenantCode";

  private final GatewayJwtProperties properties;
  private final JWTSigner signer;

  public JwtTokenUtils(GatewayJwtProperties properties) {
    if (properties == null || StrUtil.isBlank(properties.getSecret())) {
      throw new IllegalArgumentException("gateway.jwt.secret 不能为空");
    }
    this.properties = properties;
    this.signer = JWTSignerUtil.hs256(properties.getSecret().getBytes(CharsetUtil.CHARSET_UTF_8));
  }

  /** 生成携带租户、角色和权限的 Token。 */
  public String createToken(GatewayTokenClaims claims) {
    if (claims == null || StrUtil.isBlank(claims.userId()) || StrUtil.isBlank(claims.tenantCode())) {
      throw new IllegalArgumentException("用户和租户编码不能为空");
    }
    long now = System.currentTimeMillis();
    return JWT.create()
        .setIssuer(properties.getIssuer())
        .setSubject(claims.userId())
        .setIssuedAt(new Date(now))
        .setExpiresAt(new Date(now + properties.getExpireSeconds() * 1000))
        .setPayload(TENANT_CODE, claims.tenantCode())
        .setSigner(signer)
        .sign();
  }

  /** 从 Authorization 请求头提取 Bearer Token。 */
  public String resolveBearerToken(String authorization) {
    if (StrUtil.isBlank(authorization) || !authorization.startsWith(GatewayHeaders.BEARER_PREFIX)) {
      return null;
    }
    return StrUtil.trim(authorization.substring(GatewayHeaders.BEARER_PREFIX.length()));
  }

  /** 校验签名、签发方和过期时间，失败时返回 null。 */
  public GatewayTokenClaims parseToken(String token) {
    if (StrUtil.isBlank(token)) {
      return null;
    }
    try {
      JWT jwt = JWT.of(token).setSigner(signer);
      if (!jwt.verify() || !jwt.validate(0)
          || !StrUtil.equals(properties.getIssuer(),
              Convert.toStr(jwt.getPayload(RegisteredPayload.ISSUER)))) {
        return null;
      }
      return new GatewayTokenClaims(
          Convert.toStr(jwt.getPayload(RegisteredPayload.SUBJECT)),
          Convert.toStr(jwt.getPayload(TENANT_CODE)));
    } catch (RuntimeException ex) {
      return null;
    }
  }
}
