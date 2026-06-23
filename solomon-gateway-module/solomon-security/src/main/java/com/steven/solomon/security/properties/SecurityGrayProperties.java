package com.steven.solomon.security.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 灰度发布配置属性。
 *
 * <p>配置前缀 {@code security.gray}。Servlet 服务读取网关写入的灰度版本头，
 * 按版本选择不同的处理逻辑或数据源。也可由服务自身做分桶（不依赖网关）。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "security.gray")
@Validated
public class SecurityGrayProperties {

    /** 是否启用灰度发布。 */
    private boolean enabled = false;

    /** 灰度版本头名称（与网关 gateway.gray.header-name 保持一致）。 */
    @NotBlank(message = "security.gray.header-name must not be blank")
    private String headerName = "X-Gray-Version";

    /** 稳定版本标识。 */
    @NotBlank(message = "security.gray.stable-version must not be blank")
    private String stableVersion = "stable";

    /** 候选（灰度）版本标识。 */
    @NotBlank(message = "security.gray.candidate-version must not be blank")
    private String candidateVersion = "gray";

    /** 候选版本流量占比（0-100）。 */
    @Min(value = 0, message = "security.gray.candidate-weight must be at least 0")
    @Max(value = 100, message = "security.gray.candidate-weight must be at most 100")
    private int candidateWeight = 0;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }
    public String getStableVersion() { return stableVersion; }
    public void setStableVersion(String stableVersion) { this.stableVersion = stableVersion; }
    public String getCandidateVersion() { return candidateVersion; }
    public void setCandidateVersion(String candidateVersion) { this.candidateVersion = candidateVersion; }
    public int getCandidateWeight() { return candidateWeight; }
    public void setCandidateWeight(int candidateWeight) { this.candidateWeight = candidateWeight; }

    /**
     * 校验灰度权重合法性。
     */
    public void validate() {
        if (candidateWeight < 0 || candidateWeight > 100) {
            throw new IllegalArgumentException(
                    "security.gray.candidate-weight 必须在 0 到 100 之间，当前值: " + candidateWeight);
        }
    }
}
