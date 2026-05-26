package com.steven.solomon.consumer;

import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mqtt.AbstractMqttConsumerSupport;
import org.dromara.mica.mqtt.codec.message.MqttPublishMessage;
import org.dromara.mica.mqtt.core.client.IMqttClientMessageListener;
import org.tio.core.ChannelContext;

/**
 * Mica MQTT 消费者基类。
 *
 * <p>客户端适配只保留消息入口，通用消费流程由 SDK 公共模板处理。</p>
 */
public abstract class AbstractConsumer<T, R>
    extends AbstractMqttConsumerSupport<T, R, MqttMessageModel<T>>
    implements IMqttClientMessageListener {

  @SuppressWarnings("unchecked")
  @Override
  protected Class<MqttMessageModel<T>> messageModelType() {
    return (Class<MqttMessageModel<T>>) (Class<?>) MqttMessageModel.class;
  }

  @Override
  public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload) {
    try {
      consumeMessage(topic, payload, 0);
    } catch (Exception e) {
      logger.error("Mica MQTT 消费失败, topic:{}", topic, e);
    }
  }
}
