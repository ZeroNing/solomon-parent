package com.steven.solomon.mqtt.v5.profile;

import com.steven.solomon.context.AbstractTenantProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQTT5 多租户配置。
 *
 * <p>租户映射和启用开关由公共父类统一维护。</p>
 */
@ConfigurationProperties("mqtt")
public class TenantMqttProfile extends AbstractTenantProperties<MqttProfile> {
}
