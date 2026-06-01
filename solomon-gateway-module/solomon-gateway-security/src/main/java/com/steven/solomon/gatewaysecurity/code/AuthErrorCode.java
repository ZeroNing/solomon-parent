package com.steven.solomon.gatewaysecurity.code;

/** 多租户接口鉴权错误码。 */
public interface AuthErrorCode {

  String TOKEN_REQUIRED = "AUTH_TOKEN_REQUIRED";
  String TOKEN_INVALID = "AUTH_TOKEN_INVALID";
  String TENANT_REQUIRED = "AUTH_TENANT_REQUIRED";
  String TENANT_FORBIDDEN = "AUTH_TENANT_FORBIDDEN";
  String PERMISSION_DENIED = "AUTH_PERMISSION_DENIED";
}
