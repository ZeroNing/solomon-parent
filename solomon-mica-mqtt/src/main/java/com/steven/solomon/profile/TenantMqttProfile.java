package com.steven.solomon.profile;

import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("mqtt")
public class TenantMqttProfile {

    public Map<String, MqttClientProperties> tenant;

    //是否启用
    private boolean enabled = true;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, MqttClientProperties> getTenant() {
        return tenant;
    }

    public void setTenant(Map<String, MqttClientProperties> tenant) {
        this.tenant = tenant;
    }
}
