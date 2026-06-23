package com.steven.solomon.security.properties;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 安全配置属性。
 *
 * <p>配置前缀 {@code security}，控制忽略路径（健康检查/Swagger/登录等）。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "security")
@Validated
public class SecurityProperties {

    /** 是否启用安全过滤。 */
    private boolean enabled = true;

    /** 完全跳过鉴权的路径模式（健康检查、Swagger 等）。 */
    private List<@NotBlank(message = "security.ignored-paths item must not be blank") String> ignoredPaths = new ArrayList<>(List.of(
            "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**"));

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<String> getIgnoredPaths() { return ignoredPaths; }
    public void setIgnoredPaths(List<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths != null ? ignoredPaths : new ArrayList<>();
    }
}
