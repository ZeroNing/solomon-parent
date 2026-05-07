package com.steven.solomon.service;

import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;

import java.util.List;

public interface MqttInitService {

    void initMqttClient(String tenantCode, MqttClientProperties mqttProfile) throws Exception;

    void initMqttClient(String tenantCode, MqttClientProperties mqttProfile, List<Object> clazzList) throws Exception;
}
