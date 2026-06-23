package com.steven.solomon.clamav.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import xyz.capybara.clamav.Platform;

/**
 * ClamAV 病毒扫描配置属性。
 * <p>
 * 对应配置文件中的 {@code clamav.*} 前缀配置项，
 * 用于配置 ClamAV 服务器的主机地址、端口、启用状态和运行平台。
 * </p>
 *
 * @author 创建者
 */
@ConfigurationProperties("clamav")
@Validated
public class ClamAvProperties {

    /** ClamAV 服务器主机地址。 */
    private String host;

    /** ClamAV 服务器端口号。 */
    @Min(value = 1, message = "clamav.port must be between 1 and 65535")
    @Max(value = 65535, message = "clamav.port must be between 1 and 65535")
    private Integer port;

    /** 是否启用 ClamAV 病毒扫描，默认不启用。 */
    private boolean enabled = false;

    /** ClamAV 运行平台，默认为 UNIX。 */
    @NotNull(message = "clamav.platform must not be null")
    private Platform platform = Platform.UNIX;

    @AssertTrue(message = "clamav.host and clamav.port are required when clamav.enabled=true")
    public boolean isEnabledConfigurationValid() {
        return !enabled || (hasText(host) && port != null);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 获取运行平台。
     *
     * @return 平台类型
     */
    public Platform getPlatform() { return platform; }

    /**
     * 设置运行平台。
     *
     * @param platform 平台类型
     */
    public void setPlatform(Platform platform) { this.platform = platform; }

    /**
     * 判断是否启用 ClamAV。
     *
     * @return 启用状态
     */
    public boolean getEnabled() { return enabled; }

    /**
     * 设置是否启用 ClamAV。
     *
     * @param enabled 启用状态
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * 获取 ClamAV 服务器主机地址。
     *
     * @return 主机地址
     */
    public String getHost() { return host; }

    /**
     * 设置 ClamAV 服务器主机地址。
     *
     * @param host 主机地址
     */
    public void setHost(String host) { this.host = host; }

    /**
     * 获取 ClamAV 服务器端口号。
     *
     * @return 端口号
     */
    public Integer getPort() { return port; }

    /**
     * 设置 ClamAV 服务器端口号。
     *
     * @param port 端口号
     */
    public void setPort(Integer port) { this.port = port; }
}
