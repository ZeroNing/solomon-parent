package com.steven.solomon.code;

/**
 * MQTT 错误码
 */
public interface MqttErrorCode extends BaseExceptionCode {

    String MQTT_CLIENT_IS_NULL = "MQTT_CLIENT_IS_NULL";
    String MQTT_SEND_MESSAGE_ERROR = "MQTT_SEND_MESSAGE_ERROR";
    String MQTT_SUBSCRIBE_ERROR = "MQTT_SUBSCRIBE_ERROR";
    String MQTT_CONNECT_ERROR = "MQTT_CONNECT_ERROR";

}
