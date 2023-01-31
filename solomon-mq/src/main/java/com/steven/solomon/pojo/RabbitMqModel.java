package com.steven.solomon.pojo;

/**
 * 基础发送MQ基类
 */
public class RabbitMqModel<T> extends BaseMq {

  private static final long   serialVersionUID = -5799719724717056583L;
  /**
   * 交换机
   */
  private              String exchange;
  /**
   * 路由规则
   */
  private              String routingKey;
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

  public RabbitMqModel(String exchange, T body) {
    super();
    this.body = body;
    this.exchange = exchange;
  }

  public RabbitMqModel(String exchange, String routingKey, T body) {
    super();
    this.exchange   = exchange;
    this.routingKey = routingKey;
    this.body       = body;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }

}
