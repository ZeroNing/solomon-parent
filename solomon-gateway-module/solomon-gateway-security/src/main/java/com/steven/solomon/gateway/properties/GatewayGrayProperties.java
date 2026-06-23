package com.steven.solomon.gateway.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 网关灰度发布配置属性。
 *
 * <p>配置前缀 {@code gateway.gray}，控制灰度开关、版本头名称和分桶权重。
 * 网关按租户、用户和请求路径稳定分桶，将候选版本写入可信头，下游路由据此选择实例。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "gateway.gray")
@Validated
public class GatewayGrayProperties {

    /** 是否启用灰度发布。 */
    private boolean enabled = false;

    /** 灰度版本头名称，网关写入此头标识稳定版/候选版。 */
    @NotBlank(message = "gateway.gray.header-name must not be blank")
    private String headerName = "X-Gray-Version";

    /** 稳定版本标识。 */
    @NotBlank(message = "gateway.gray.stable-version must not be blank")
    private String stableVersion = "stable";

    /** 候选（灰度）版本标识。 */
    @NotBlank(message = "gateway.gray.candidate-version must not be blank")
    private String candidateVersion = "gray";

    /** 候选版本流量占比（0-100），100 表示全部走候选。 */
    @Min(value = 0, message = "gateway.gray.candidate-weight must be at least 0")
    @Max(value = 100, message = "gateway.gray.candidate-weight must be at most 100")
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
