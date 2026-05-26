package com.steven.solomon.service;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.dromara.mica.mqtt.core.client.MqttClient;
import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.slf4j.Logger;

import java.util.List;

public class DefaultMqttInitService implements MqttClientInitService<MqttClientProperties> {

    private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

    private final MqttUtils utils;

    public DefaultMqttInitService(MqttUtils utils) {
        this.utils = utils;
    }

    @Override
    public void initMqttClient(String tenantCode, MqttClientProperties mqttProfile) throws Exception {
        initMqttClient(tenantCode, mqttProfile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
    }

    @Override
    public void initMqttClient(String tenantCode, MqttClientProperties mqttProfile, List<Object> clazzList) throws Exception {
        // 创建 MQTT 客户端
        MqttClient mqttClient = utils.createMqttClient(tenantCode, mqttProfile);
        
        // 订阅主题
        utils.subscribe(mqttClient, clazzList, tenantCode);
        
        logger.info("MQTT客户端初始化成功，租户: {}, URL: {}", tenantCode, mqttProfile.getIp() + ":" + mqttProfile.getPort());
    }
}
