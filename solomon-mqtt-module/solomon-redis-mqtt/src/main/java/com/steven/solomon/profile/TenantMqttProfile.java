package com.steven.solomon.profile;

import com.steven.solomon.mqtt.model.AbstractTenantMqttProfile;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis MQTT 多租户配置。
 */
@ConfigurationProperties("mqtt")
public class TenantMqttProfile extends AbstractTenantMqttProfile<RedisMqttProfile> {
}
