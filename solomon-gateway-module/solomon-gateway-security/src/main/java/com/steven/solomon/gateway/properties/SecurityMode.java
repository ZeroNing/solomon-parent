package com.steven.solomon.gateway.properties;

/**
 * 网关部署模式。
 *
 * @author steven
 */
public enum SecurityMode {

    /** 微服务模式：向下游透传可信租户与用户头。 */
    MICROSERVICE,

    /** 单机模式：身份仅保留在网关上下文，不外发。 */
    STANDALONE
}
