package com.steven.solomon.gateway.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 网关 Swagger 聚合配置属性。
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "gateway.swagger")
@Validated
public class GatewaySwaggerProperties {

    /** 是否启用 Swagger 聚合。 */
    private boolean enabled = true;

    /** 各服务的 api-docs 路径后缀。 */
    @NotBlank(message = "gateway.swagger.api-docs-path must not be blank")
    private String apiDocsPath = "/v3/api-docs";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiDocsPath() {
        return apiDocsPath;
    }

    public void setApiDocsPath(String apiDocsPath) {
        this.apiDocsPath = apiDocsPath;
    }
}
