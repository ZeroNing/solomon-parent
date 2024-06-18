package com.steven.solomon.service;

import com.steven.solomon.profile.MqttProfile;

public interface MqttInitService {

    void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception;
}
