package com.steven.solomon.mqtt.vertx.profile;

import com.steven.solomon.context.AbstractTenantProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Vert.x MQTT 多租户配置。
 *
 * <p>租户映射和启用开关由公共父类统一维护。</p>
 */
@ConfigurationProperties("mqtt")
@Validated
public class TenantMqttProfile extends AbstractTenantProperties<MqttProfile> {
}
