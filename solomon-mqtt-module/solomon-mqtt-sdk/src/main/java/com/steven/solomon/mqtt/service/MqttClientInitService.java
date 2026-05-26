package com.steven.solomon.mqtt.service;

import java.util.List;

/**
 * MQTT 客户端初始化服务基础接口。
 *
 * @param <P> MQTT 客户端配置类型
 */
public interface MqttClientInitService<P> {

  /**
   * 初始化指定租户的 MQTT 客户端。
   *
   * @param tenantCode 租户编码
   * @param mqttProfile MQTT 客户端配置
   */
  void initMqttClient(String tenantCode, P mqttProfile) throws Exception;

  /**
   * 初始化指定租户的 MQTT 客户端，并传入已扫描到的监听器实例。
   *
   * @param tenantCode 租户编码
   * @param mqttProfile MQTT 客户端配置
   * @param clazzList 监听器实例列表
   */
  void initMqttClient(String tenantCode, P mqttProfile, List<Object> clazzList) throws Exception;
}
