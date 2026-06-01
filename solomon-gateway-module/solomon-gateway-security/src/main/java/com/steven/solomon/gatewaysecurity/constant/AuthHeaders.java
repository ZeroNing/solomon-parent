package com.steven.solomon.gatewaysecurity.constant;

/** 鉴权请求头常量。 */
public interface AuthHeaders {

  String AUTHORIZATION = "Authorization";
  String TENANT_CODE = "X-Tenant-Code";
  String USER_ID = "X-User-Id";
  String BEARER_PREFIX = "Bearer ";
}
