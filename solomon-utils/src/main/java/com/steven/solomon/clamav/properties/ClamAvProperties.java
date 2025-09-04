package com.steven.solomon.clamav.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import xyz.capybara.clamav.Platform;

@ConfigurationProperties("clamav")
public class ClamAvProperties {

    private String host;

    private Integer port;

    private boolean enabled = false;

    private Platform platform = Platform.UNIX;

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
