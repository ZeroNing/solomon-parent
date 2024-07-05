package com.steven.solomon.entity;
import com.steven.solomon.pojo.entity.BaseMq;

/**
 * 基础发送MQ基类
 */
public class MqttModel<T> extends BaseMq<T> {

  private String topic;

  private boolean retained;

  private int qos;

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

  public MqttModel(String tenantCode) {
    super();
    setTenantCode(tenantCode);
  }

  public MqttModel(String tenantCode, String topic, T body) {
    super(body);
    this.topic = topic;
    setTenantCode(tenantCode);
  }

}
