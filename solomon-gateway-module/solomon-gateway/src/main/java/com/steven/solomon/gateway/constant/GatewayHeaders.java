package com.steven.solomon.gateway.constant;

import com.steven.solomon.code.BaseCode;

/** 网关向下游透传的可信请求头。 */
public interface GatewayHeaders {

  String AUTHORIZATION = "Authorization";
  String TENANT_CODE = BaseCode.TENANT_CODE;
  String USER_ID = BaseCode.USER_ID;
  String BEARER_PREFIX = "Bearer ";
}
