package com.steven.solomon.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 网关鉴权部署配置。 */
@ConfigurationProperties("gateway.security")
public class GatewaySecurityProperties {

  /** 默认适配微服务网关，单机应用可切换为 STANDALONE。 */
  private GatewaySecurityMode mode = GatewaySecurityMode.MICROSERVICE;

  /** 是否透传可信身份头；未配置时按部署模式自动决定。 */
  private Boolean forwardTrustedHeaders;

  public GatewaySecurityMode getMode() {
    return mode;
  }

  public void setMode(GatewaySecurityMode mode) {
    this.mode = mode;
  }

  public Boolean getForwardTrustedHeaders() {
    return forwardTrustedHeaders;
  }

  public void setForwardTrustedHeaders(Boolean forwardTrustedHeaders) {
    this.forwardTrustedHeaders = forwardTrustedHeaders;
  }

  public boolean shouldForwardTrustedHeaders() {
    return forwardTrustedHeaders != null
        ? forwardTrustedHeaders : GatewaySecurityMode.MICROSERVICE.equals(mode);
  }
}
