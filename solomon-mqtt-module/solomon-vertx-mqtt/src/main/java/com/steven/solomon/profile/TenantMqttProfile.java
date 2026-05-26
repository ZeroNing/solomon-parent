package com.steven.solomon.profile;

import com.steven.solomon.mqtt.model.AbstractTenantMqttProfile;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vert.x MQTT 多租户配置。
 *
 * <p>租户映射和启用开关由公共父类统一维护。</p>
 */
@ConfigurationProperties("mqtt")
public class TenantMqttProfile extends AbstractTenantMqttProfile<MqttProfile> {
}
