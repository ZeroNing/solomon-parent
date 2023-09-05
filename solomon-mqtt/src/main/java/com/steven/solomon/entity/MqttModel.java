package com.steven.solomon.entity;


import com.steven.solomon.pojo.entity.BaseMq;

/**
 * 基础发送MQ基类
 */
public class MqttModel<T> extends BaseMq<T> {

  private String topic;

  private String tenantCode;

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public void setTenantCode(String tenantCode) {
    this.tenantCode = tenantCode;
  }

  public MqttModel(String tenantCode) {
    super();
    this.tenantCode = tenantCode;
  }

  public MqttModel(String tenantCode,String topic,T body) {
    super(body);
    this.topic = topic;
    this.tenantCode = tenantCode;
  }

}
