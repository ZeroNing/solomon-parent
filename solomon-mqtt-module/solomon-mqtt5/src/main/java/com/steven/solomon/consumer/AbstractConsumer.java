package com.steven.solomon.consumer;

import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mqtt.AbstractMqttConsumerSupport;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.common.MqttMessage;

/**
 * Paho MQTT5 消费者基类。
 *
 * <p>客户端适配只保留消息入口，通用消费流程由公共模板处理。</p>
 */
public abstract class AbstractConsumer<T, R>
    extends AbstractMqttConsumerSupport<T, R, MqttMessageModel<T>>
    implements IMqttMessageListener {

  protected MqttMessage mqttMessage;

  @SuppressWarnings("unchecked")
  @Override
  protected Class<MqttMessageModel<T>> messageModelType() {
    return (Class<MqttMessageModel<T>>) (Class<?>) MqttMessageModel.class;
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    this.mqttMessage = message;
    consumeMessage(topic, message.getPayload(), message.getId());
  }
}
