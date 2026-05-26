package com.steven.solomon.mqtt.code;

import com.steven.solomon.code.BaseExceptionCode;

/**
 * MQTT 公共错误码。
 */
public interface MqttErrorCodes extends BaseExceptionCode {

  /**
   * MQTT 客户端未初始化或不可用。
   */
  String CLIENT_IS_NULL = "CLIENT_IS_NULL";

  /**
   * MQTT 客户端未初始化或不可用。
   */
  String MQTT_CLIENT_IS_NULL = "MQTT_CLIENT_IS_NULL";

  /**
   * MQTT 消息发送失败。
   */
  String MQTT_SEND_MESSAGE_ERROR = "MQTT_SEND_MESSAGE_ERROR";

  /**
   * MQTT 主题订阅失败。
   */
  String MQTT_SUBSCRIBE_ERROR = "MQTT_SUBSCRIBE_ERROR";

  /**
   * MQTT 客户端连接失败。
   */
  String MQTT_CONNECT_ERROR = "MQTT_CONNECT_ERROR";
}
