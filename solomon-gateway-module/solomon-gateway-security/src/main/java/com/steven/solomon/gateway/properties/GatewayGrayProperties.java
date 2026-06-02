package com.steven.solomon.gateway.properties;

import cn.hutool.core.util.StrUtil;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 网关灰度发布配置。 */
@ConfigurationProperties("gateway.gray")
public class GatewayGrayProperties {

  private static final Pattern HEADER_NAME_PATTERN =
      Pattern.compile("[!#$%&'*+.^_`|~0-9A-Za-z-]+");
  private static final Pattern VERSION_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,64}");

  /** 是否启用灰度发布分桶。 */
  private boolean enabled;

  /** 网关向下游写入的可信灰度版本请求头。 */
  private String headerName = "X-Gray-Version";

  /** 稳定版本标识。 */
  private String stableVersion = "stable";

  /** 灰度版本标识。 */
  private String candidateVersion = "gray";

  /** 灰度流量百分比，取值范围为 0 到 100。 */
  private int candidateWeight;

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

  /** 校验灰度请求头与版本标识，避免生成不安全的下游请求头。 */
  public void validate() {
    if (StrUtil.isBlank(headerName) || !HEADER_NAME_PATTERN.matcher(headerName).matches()) {
      throw new IllegalArgumentException("gateway.gray.header-name 格式不正确");
    }
    if (!isSafeVersion(stableVersion) || !isSafeVersion(candidateVersion)) {
      throw new IllegalArgumentException("gateway.gray 版本标识格式不正确");
    }
    if (candidateWeight < 0 || candidateWeight > 100) {
      throw new IllegalArgumentException("gateway.gray.candidate-weight 必须在 0 到 100 之间");
    }
  }

  private boolean isSafeVersion(String version) {
    return StrUtil.isNotBlank(version) && VERSION_PATTERN.matcher(version).matches();
  }
}
