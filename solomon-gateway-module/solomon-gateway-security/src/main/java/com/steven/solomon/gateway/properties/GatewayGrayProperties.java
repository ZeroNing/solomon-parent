package com.steven.solomon.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关灰度发布配置属性。
 *
 * <p>配置前缀 {@code gateway.gray}，控制灰度开关、版本头名称和分桶权重。
 * 网关按租户、用户和请求路径稳定分桶，将候选版本写入可信头，下游路由据此选择实例。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "gateway.gray")
public class GatewayGrayProperties {

    /** 是否启用灰度发布。 */
    private boolean enabled = false;

    /** 灰度版本头名称，网关写入此头标识稳定版/候选版。 */
    private String headerName = "X-Gray-Version";

    /** 稳定版本标识。 */
    private String stableVersion = "stable";

    /** 候选（灰度）版本标识。 */
    private String candidateVersion = "gray";

    /** 候选版本流量占比（0-100），100 表示全部走候选。 */
    private int candidateWeight = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getStableVersion() {
        return stableVersion;
    }

    public void setStableVersion(String stableVersion) {
        this.stableVersion = stableVersion;
    }

    public String getCandidateVersion() {
        return candidateVersion;
    }

    public void setCandidateVersion(String candidateVersion) {
        this.candidateVersion = candidateVersion;
    }

    public int getCandidateWeight() {
        return candidateWeight;
    }

    public void setCandidateWeight(int candidateWeight) {
        this.candidateWeight = candidateWeight;
    }

    /**
     * 校验灰度权重合法性。
     *
     * @throws IllegalArgumentException 当 candidateWeight 不在 0-100 范围内时
     */
    public void validate() {
        if (candidateWeight < 0 || candidateWeight > 100) {
            throw new IllegalArgumentException(
                    "gateway.gray.candidate-weight 必须在 0 到 100 之间，当前值: " + candidateWeight);
        }
    }
}
