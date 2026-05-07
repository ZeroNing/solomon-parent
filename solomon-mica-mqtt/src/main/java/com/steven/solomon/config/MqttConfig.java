package com.steven.solomon.config;

import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.init.AbstractMessageLineRunner;
import com.steven.solomon.profile.TenantMqttProfile;
import com.steven.solomon.service.MqttInitService;
import com.steven.solomon.service.impl.DefaultMqttInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Configuration
@EnableConfigurationProperties(value = {TenantMqttProfile.class})
@Import(value = {MqttUtils.class})
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttConfig extends AbstractMessageLineRunner<MessageListener> {

    private final TenantMqttProfile profile;

    private final MqttUtils mqttUtils;

    public MqttConfig(TenantMqttProfile profile, ApplicationContext applicationContext, MqttUtils mqttUtils) {
        this.profile = profile;
        this.mqttUtils = mqttUtils;
        SpringUtil.setContext(applicationContext);
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if (!profile.getEnabled()) {
            logger.error("mqtt不启用,不初始化队列以及消费者");
            return;
        }
        Map<String, MqttClientProperties> tenantProfileMap = profile.getTenant();
        if (ValidateUtils.isEmpty(tenantProfileMap)) {
            logger.error("AbstractMessageLineRunner:没有MQTT配置");
            return;
        }
        Map<String, MqttInitService> abstractMQMap = SpringUtil.getBeansOfType(MqttInitService.class);
        MqttInitService mqttInitService = ValidateUtils.isNotEmpty(abstractMQMap) 
                ? abstractMQMap.values().stream().findFirst().get() 
                : new DefaultMqttInitService(mqttUtils);
        for (Entry<String, MqttClientProperties> entry : tenantProfileMap.entrySet()) {
            mqttInitService.initMqttClient(entry.getKey(), entry.getValue(), clazzList);
        }
    }
}
