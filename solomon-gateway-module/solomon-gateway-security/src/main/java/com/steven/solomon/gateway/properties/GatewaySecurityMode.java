package com.steven.solomon.gateway.properties;

/** 网关鉴权部署模式。 */
public enum GatewaySecurityMode {

  /** 微服务模式，向下游服务透传网关校验后的可信身份头。 */
  MICROSERVICE,

  /** 单机模式，仅保留网关交换上下文中的可信身份。 */
  STANDALONE
}
