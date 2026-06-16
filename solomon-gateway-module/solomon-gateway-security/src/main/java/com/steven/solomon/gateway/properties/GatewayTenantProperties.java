package com.steven.solomon.gateway.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关多租户配置属性。
 *
 * <p>配置前缀 {@code gateway.tenant}，控制租户校验开关、忽略路径和公开租户路径。</p>
 *
 * <ul>
 *   <li>{@code ignored-paths}：完全跳过鉴权的路径（如健康检查、Swagger），
 *       网关会清理这些路径上的所有不可信头。</li>
 *   <li>{@code public-tenant-paths}：鉴权前需校验租户的公开路径（如登录接口），
 *       允许无 Token 访问但必须携带合法租户编码。<b>必须同时配置在 ignored-paths 中。</b></li>
 * </ul>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "gateway.tenant")
public class GatewayTenantProperties {

    /** 是否启用租户校验。 */
    private boolean enabled = true;

    /** 完全跳过鉴权的路径模式列表。 */
    private List<String> ignoredPaths = new ArrayList<>(List.of(
            "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**"));

    /** 公开租户路径列表（登录等，需校验租户但不需要 Token）。 */
    private List<String> publicTenantPaths = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIgnoredPaths() {
        return ignoredPaths;
    }

    public void setIgnoredPaths(List<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths != null ? ignoredPaths : new ArrayList<>();
    }

    public List<String> getPublicTenantPaths() {
        return publicTenantPaths;
    }

    public void setPublicTenantPaths(List<String> publicTenantPaths) {
        this.publicTenantPaths = publicTenantPaths != null ? publicTenantPaths : new ArrayList<>();
    }

    /**
     * 校验配置合法性。
     *
     * @throws IllegalArgumentException 当 publicTenantPaths 含有不在 ignoredPaths 中的路径时
     */
    public void validate() {
        if (publicTenantPaths == null || publicTenantPaths.isEmpty()) {
            return;
        }
        for (String publicPath : publicTenantPaths) {
            if (ignoredPaths == null || !ignoredPaths.contains(publicPath)) {
                throw new IllegalArgumentException(
                        "公开租户路径 " + publicPath + " 必须同时配置在 ignored-paths 中");
            }
        }
    }
}
