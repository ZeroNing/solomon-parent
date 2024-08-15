package com.steven.solomon.service;

import com.steven.solomon.profile.MqttProfile;

import java.util.List;

public interface MqttInitService {

    void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> clazzList) throws Exception;

    void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception;
}
