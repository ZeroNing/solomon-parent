package com.steven.solomon.config;

import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.init.AbstractMessageLineRunner;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.profile.TenantMqttProfile;
import com.steven.solomon.service.MqttInitService;
import com.steven.solomon.service.impl.DefaultMqttInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.verification.ValidateUtils;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
        // 检查 MQTT 是否启用
        if (!profile.getEnabled()) {
            logger.error("mqtt 不启用，不初始化队列以及消费者");
            return;
        }
        // 获取所有租户的配置 Map<租户编码，MQTT 配置>
        Map<String, MqttProfile> tenantProfileMap = profile.getTenant();
        if (ValidateUtils.isEmpty(tenantProfileMap)) {
            logger.error("AbstractMessageLineRunner:没有 MQTT 配置");
            return;
        }
        // 获取 MqttInitService Bean，如果不存在则使用默认实现
        Map<String, MqttInitService> abstractMQMap = SpringUtil.getBeansOfType(MqttInitService.class);
        MqttInitService mqttInitService = ValidateUtils.isNotEmpty(abstractMQMap) 
                ? abstractMQMap.values().stream().findFirst().get() 
                : new DefaultMqttInitService(mqttUtils);
        // 遍历所有租户配置，为每个租户初始化 MQTT Client
        for (Map.Entry<String, MqttProfile> entry : tenantProfileMap.entrySet()) {
            // entry.getKey()=租户编码，entry.getValue()=该租户的 MQTT 配置
            mqttInitService.initMqttClient(entry.getKey(), entry.getValue(), clazzList);
        }
    }
}
