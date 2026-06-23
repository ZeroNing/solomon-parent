package com.steven.solomon.mqtt.mica.profile;

import com.steven.solomon.context.AbstractTenantProperties;
import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Mica MQTT 多租户配置。
 *
 * <p>租户映射和启用开关由 SDK 公共父类统一维护。</p>
 */
@ConfigurationProperties("mqtt")
@Validated
public class TenantMqttProfile extends AbstractTenantProperties<MqttClientProperties> {
}
