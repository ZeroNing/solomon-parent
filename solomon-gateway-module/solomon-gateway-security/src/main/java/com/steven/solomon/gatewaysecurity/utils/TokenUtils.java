package com.steven.solomon.gatewaysecurity.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.gatewaysecurity.code.AuthErrorCode;
import com.steven.solomon.gatewaysecurity.constant.AuthHeaders;
import com.steven.solomon.gatewaysecurity.model.AccessTokenClaims;
import com.steven.solomon.gatewaysecurity.properties.AuthProperties;
import java.util.Date;
import java.util.List;

/** JWT 生成与解析工具。 */
public class TokenUtils {

  private static final String TENANT_CODE = "tenantCode";
  private static final String PERMISSIONS = "permissions";

  private final AuthProperties properties;
  private final JWTSigner signer;

  public TokenUtils(AuthProperties properties) {
    if (properties == null || StrUtil.isBlank(properties.getSecret())) {
      throw new IllegalArgumentException("gateway.security.secret 不能为空");
    }
    this.properties = properties;
    this.signer = JWTSignerUtil.hs256(properties.getSecret().getBytes(CharsetUtil.CHARSET_UTF_8));
  }

  /** 生成携带租户和接口权限的 Token。 */
  public String createToken(AccessTokenClaims claims) {
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
        .setPayload(PERMISSIONS, claims.permissions())
        .setSigner(signer)
        .sign();
  }

  /** 从 Authorization 请求头中提取 Bearer Token。 */
  public String resolveBearerToken(String authorization) {
    if (StrUtil.isBlank(authorization) || !authorization.startsWith(AuthHeaders.BEARER_PREFIX)) {
      return null;
    }
    return StrUtil.trim(authorization.substring(AuthHeaders.BEARER_PREFIX.length()));
  }

  /** 解析并校验 Token，失败时统一抛出 BaseException。 */
  public AccessTokenClaims parseToken(String token) throws BaseException {
    if (StrUtil.isBlank(token)) {
      throw new BaseException(AuthErrorCode.TOKEN_REQUIRED);
    }
    try {
      JWT jwt = JWT.of(token).setSigner(signer);
      if (!jwt.verify() || !jwt.validate(0)
          || !StrUtil.equals(properties.getIssuer(),
              Convert.toStr(jwt.getPayload(RegisteredPayload.ISSUER)))) {
        throw new BaseException(AuthErrorCode.TOKEN_INVALID);
      }
      return new AccessTokenClaims(
          Convert.toStr(jwt.getPayload(RegisteredPayload.SUBJECT)),
          Convert.toStr(jwt.getPayload(TENANT_CODE)),
          Convert.toList(String.class, jwt.getPayload(PERMISSIONS)));
    } catch (BaseException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new BaseException(AuthErrorCode.TOKEN_INVALID, ex);
    }
  }

  /** 解析可选 Token，公开接口使用；无 Token 或 Token 无效时不报错。 */
  public AccessTokenClaims parseOptionalToken(String token) {
    if (StrUtil.isBlank(token)) {
      return null;
    }
    try {
      return parseToken(token);
    } catch (BaseException ex) {
      return null;
    }
  }
}
