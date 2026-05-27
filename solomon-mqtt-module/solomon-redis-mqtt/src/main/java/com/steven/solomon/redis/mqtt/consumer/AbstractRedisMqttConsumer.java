package com.steven.solomon.redis.mqtt.consumer;

import com.steven.solomon.mqtt.AbstractMqttConsumerSupport;
import com.steven.solomon.mqtt.model.MqttMessageModel;
import java.nio.charset.StandardCharsets;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * Redis MQTT 消费者基类。
 */
public abstract class AbstractRedisMqttConsumer<T, R>
    extends AbstractMqttConsumerSupport<T, R, MqttMessageModel<T>>
    implements MessageListener {

  @SuppressWarnings("unchecked")
  @Override
  protected Class<MqttMessageModel<T>> messageModelType() {
    return (Class<MqttMessageModel<T>>) (Class<?>) MqttMessageModel.class;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
    try {
      consumeMessage(channel, message.getBody());
    } catch (Exception e) {
      logger.error("Redis MQTT 消费失败, channel={}", channel, e);
    }
  }
}
