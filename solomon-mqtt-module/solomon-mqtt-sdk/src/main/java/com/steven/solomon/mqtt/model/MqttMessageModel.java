package com.steven.solomon.mqtt.model;

import com.steven.solomon.pojo.entity.BaseMq;

/**
 * MQTT 消息基础模型，统一维护各 MQTT 实现共用的主题、QoS 和保留消息标记。
 *
 * @param <T> 消息体类型
 */
public class MqttMessageModel<T> extends BaseMq<T> {

  /**
   * MQTT 主题。
   */
  private String topic;

  /**
   * 是否发送为 retained 消息。
   */
  private boolean retained;

  /**
   * MQTT 消息质量等级。
   */
  private int qos;

  public MqttMessageModel() {
    super();
  }

  public MqttMessageModel(String tenantCode) {
    super();
    setTenantCode(tenantCode);
  }

  public MqttMessageModel(String tenantCode, String topic, T body) {
    super(body);
    this.topic = topic;
    setTenantCode(tenantCode);
  }

  public int getQos() {
    return qos;
  }

  public void setQos(int qos) {
    this.qos = qos;
  }

  public boolean getRetained() {
    return retained;
  }

  public void setRetained(boolean retained) {
    this.retained = retained;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
}
