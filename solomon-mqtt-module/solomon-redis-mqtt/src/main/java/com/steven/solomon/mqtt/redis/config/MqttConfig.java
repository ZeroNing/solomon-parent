package com.steven.solomon.mqtt.redis.config;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.config.AbstractMqttTenantLineRunner;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.mqtt.redis.profile.RedisMqttProfile;
import com.steven.solomon.mqtt.redis.profile.TenantMqttProfile;
import com.steven.solomon.mqtt.redis.service.DefaultMqttInitService;
import com.steven.solomon.mqtt.redis.utils.MqttUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Redis MQTT 自动配置。
 */
@Configuration
@EnableConfigurationProperties(TenantMqttProfile.class)
@Import(MqttUtils.class)
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttConfig
    extends AbstractMqttTenantLineRunner<MessageListener, RedisMqttProfile, MqttClientInitService<RedisMqttProfile>> {

  public MqttConfig(TenantMqttProfile profile, ApplicationContext applicationContext, MqttUtils mqttUtils) {
    super(MessageListener.class, profile, applicationContext, MqttClientInitService.class,
        () -> new DefaultMqttInitService(mqttUtils));
  }
}
