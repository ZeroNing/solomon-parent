package com.steven.solomon.gateway.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 网关安全配置属性。
 *
 * <p>配置前缀 {@code gateway.security}，控制部署模式（微服务/单机）和是否向下游
 * 透传可信身份头。</p>
 *
 * <ul>
 *   <li>{@link SecurityMode#MICROSERVICE}：微服务模式，向下游写入 {@code X-Tenant-Code}、
 *       {@code X-User-Id}，下游服务据此绑定租户资源。</li>
 *   <li>{@link SecurityMode#STANDALONE}：单机模式，身份仅保留在网关上下文中，不外发身份头，
 *       适合不需要透传的单体应用。</li>
 * </ul>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "gateway.security")
@Validated
public class GatewaySecurityProperties {

    /** 部署模式，默认微服务模式。 */
    @NotNull(message = "gateway.security.mode must not be null")
    private SecurityMode mode = SecurityMode.MICROSERVICE;

    /** 是否强制向下游透传可信头。未配置时按部署模式决定。 */
    private Boolean forwardTrustedHeaders;

    public SecurityMode getMode() {
        return mode;
    }

    public void setMode(SecurityMode mode) {
        this.mode = mode;
    }

    public Boolean getForwardTrustedHeaders() {
        return forwardTrustedHeaders;
    }

    public void setForwardTrustedHeaders(Boolean forwardTrustedHeaders) {
        this.forwardTrustedHeaders = forwardTrustedHeaders;
    }

    /**
     * 判断是否应向下游透传可信身份头。
     *
     * <p>优先使用显式配置；未配置时微服务模式透传，单机模式不透传。</p>
     *
     * @return true 表示向下游写入 {@code X-Tenant-Code}、{@code X-User-Id}
     */
    public boolean shouldForwardTrustedHeaders() {
        if (forwardTrustedHeaders != null) {
            return forwardTrustedHeaders;
        }
        return mode == SecurityMode.MICROSERVICE;
    }
}
