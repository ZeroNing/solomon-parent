package com.steven.solomon.entity;

import com.steven.solomon.pojo.BaseMq;

/**
 * 基础发送MQ基类
 */
public class MqttModel<T> extends BaseMq<T> {

  private String topic;

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public MqttModel() {
    super();
  }

  public MqttModel(String topic,T body) {
    super(body);
    this.topic = topic;
  }

}
