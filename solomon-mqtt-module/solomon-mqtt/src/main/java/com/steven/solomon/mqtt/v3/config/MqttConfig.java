package com.steven.solomon.mqtt.v3.config;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.config.AbstractMqttTenantLineRunner;
import com.steven.solomon.mqtt.condition.MqttEnabledCondition;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.mqtt.v3.profile.MqttProfile;
import com.steven.solomon.mqtt.v3.profile.TenantMqttProfile;
import com.steven.solomon.mqtt.v3.service.DefaultMqttInitService;
import com.steven.solomon.mqtt.v3.utils.MqttUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

/**
 * MQTT3 自动配置。
 */
@Configuration
@IntegrationComponentScan
@EnableConfigurationProperties(TenantMqttProfile.class)
@Import(MqttUtils.class)
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttConfig
    extends AbstractMqttTenantLineRunner<MessageListener, MqttProfile, MqttClientInitService<MqttProfile>> {

  public MqttConfig(TenantMqttProfile profile, ApplicationContext applicationContext, MqttUtils mqttUtils) {
    super(MessageListener.class, profile, applicationContext, MqttClientInitService.class,
        () -> new DefaultMqttInitService(mqttUtils));
  }

  @Bean
  @ConditionalOnMissingBean(MessageChannel.class)
  @Conditional(MqttEnabledCondition.class)
  public MessageChannel mqttInputChannel() {
    return new DirectChannel();
  }
}
