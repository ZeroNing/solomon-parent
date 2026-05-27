package com.steven.solomon.mqtt;

import com.steven.solomon.mqtt.model.MqttMessageModel;

/**
 * MQTT 客户端统一操作接口。
 */
public interface MqttOperations {

  /**
   * 发送普通消息。
   */
  void send(MqttMessageModel<?> data) throws Exception;

  /**
   * 订阅主题。
   */
  void subscribe(String tenantCode, String topic, int qos, Object consumer) throws Exception;

  /**
   * 取消订阅主题。
   */
  void unsubscribe(String tenantCode, String[] topics) throws Exception;

  /**
   * 断开租户连接。
   */
  void disconnect(String tenantCode) throws Exception;

  /**
   * 重连租户连接。
   */
  void reconnect(String tenantCode) throws Exception;
}
