package com.steven.solomon.entity;

/**
 * 基础发送MQ基类
 */
public class RabbitMqModel<T>  {

  /**
   * 消费者数据
   */
  private              T body;

  public RabbitMqModel() {
    super();
  }

  public RabbitMqModel(T body) {
    super();
    this.body = body;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }

}
