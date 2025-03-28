package com.steven.solomon.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("xxl")
public class SaasXxlJobProperties {

    private Map<String,XxlJobProperties> tenant;

    private boolean enabled = true;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, XxlJobProperties> getTenant() {
        return tenant;
    }

    public void setTenant(Map<String, XxlJobProperties> tenant) {
        this.tenant = tenant;
    }
}
