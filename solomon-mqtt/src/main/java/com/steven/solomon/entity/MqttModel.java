package com.steven.solomon.entity;

import com.steven.solomon.pojo.BaseMq;

/**
 * 基础发送MQ基类
 */
public class MqttModel<T> extends BaseMq<T> {

  public MqttModel() {
    super();
  }

  public MqttModel(T body) {
    super(body);
  }

}
