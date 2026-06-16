package com.steven.solomon.mqtt.vertx.consumer;

import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mq.AbstractMessageConsumer;
import io.vertx.mqtt.messages.MqttPublishMessage;

/**
 * Vert.x MQTT 消费者基类。
 *
 * <p>客户端适配只保留消息入口，通用消费流程由公共模板处理。</p>
 */
public abstract class AbstractConsumer<T, R> extends AbstractMessageConsumer<T, R, MqttMessageModel<T>> {

  @SuppressWarnings("unchecked")
  @Override
  protected Class<MqttMessageModel<T>> messageModelType() {
    return (Class<MqttMessageModel<T>>) (Class<?>) MqttMessageModel.class;
  }

  public void messageArrived(String topic, MqttPublishMessage message) throws Exception {
    consumeMessage(topic, message.payload().getBytes());
  }
}
