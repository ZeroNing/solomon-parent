package com.steven.solomon.mqtt.model;

import com.steven.solomon.mqtt.annotation.MessageListener;

/**
 * MQTT 订阅描述。
 */
public class MqttSubscriptionDescriptor {

  private final String tenantCode;

  private final String topic;

  private final int qos;

  private final Object listener;

  private final MessageListener annotation;

  public MqttSubscriptionDescriptor(
      String tenantCode, String topic, int qos, Object listener, MessageListener annotation) {
    this.tenantCode = tenantCode;
    this.topic = topic;
    this.qos = qos;
    this.listener = listener;
    this.annotation = annotation;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public String getTopic() {
    return topic;
  }

  public int getQos() {
    return qos;
  }

  public Object getListener() {
    return listener;
  }

  public MessageListener getAnnotation() {
    return annotation;
  }
}
