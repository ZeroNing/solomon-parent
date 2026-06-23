package com.steven.solomon.mqtt.redis.profile;

import com.steven.solomon.context.AbstractTenantProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Redis MQTT 多租户配置。
 */
@ConfigurationProperties("mqtt")
@Validated
public class TenantMqttProfile extends AbstractTenantProperties<RedisMqttProfile> {
}
