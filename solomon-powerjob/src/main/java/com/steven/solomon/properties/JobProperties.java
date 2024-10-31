package com.steven.solomon.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("powerjob.worker")
public class JobProperties {

    //是否启用
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }
}
