package com.steven.solomon.gateway.code;

/** 网关鉴权错误码。 */
public interface GatewayErrorCode {

  String TOKEN_REQUIRED = "GATEWAY_TOKEN_REQUIRED";
  String TOKEN_INVALID = "GATEWAY_TOKEN_INVALID";
  String TENANT_REQUIRED = "GATEWAY_TENANT_REQUIRED";
  String ACCESS_DENIED = "GATEWAY_ACCESS_DENIED";
}
