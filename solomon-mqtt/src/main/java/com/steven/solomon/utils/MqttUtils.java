package com.steven.solomon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.annotation.Mqtt;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Resource;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class MqttUtils {

  private Logger logger = LoggerUtils.logger(MqttUtils.class);

  @Resource
  private MqttClient client;

  private MqttConnectOptions options;

  private Map<String, Map<AbstractConsumer,Mqtt>> consumer = new HashMap<>();

  public void setOptions(MqttConnectOptions options) {
    this.options = options;
  }

  public void setConsumer(Map<String, Map<AbstractConsumer,Mqtt>> consumer){
    this.consumer.putAll(consumer);
  }

  /**
   *   发送消息
   *   @param data 消息内容
   */
  public void send(MqttModel data) {
    // 获取客户端实例
    ObjectMapper mapper = new ObjectMapper();
    try {
      // 转换消息为json字符串
      String json = mapper.writeValueAsString(data);
      client.publish(data.getTopic(), new MqttMessage(json.getBytes(StandardCharsets.UTF_8)));
    } catch (JsonProcessingException e) {
      logger.error(String.format("MQTT: 主题[%s]发送消息转换json失败", data.getTopic()));
    } catch (MqttException e) {
      logger.error(String.format("MQTT: 主题[%s]发送消息失败", data.getTopic()));
    }
  }

  /**
   * 订阅消息
   * @param topic 主题
   * @param qos 消息质量
   * @param consumer 消费者
   */
  public void subscribe(String topic,int qos, AbstractConsumer consumer) throws MqttException {
    if(ValidateUtils.isEmpty(topic)){
      return;
    }
    client.subscribe(topic, qos,consumer);
  }

  /**
   * 取消订阅
   * @param topic 主题
   */
  public void unsubscribe(String[] topic) throws MqttException {
    if(ValidateUtils.isEmpty(topic)){
      return;
    }
    client.unsubscribe(topic);
  }

  /**
   * 关闭连接
   */
  public void disconnect() throws MqttException {
    client.disconnect();
  }

  /**
   * 重新连接
   */
  public void reconnect() throws MqttException {
    if(!client.isConnected()){
      client.connect(this.options);
      Map<String, Map<AbstractConsumer,Mqtt>> consumer = this.consumer;
      for(Map<AbstractConsumer,Mqtt> map : consumer.values()){
        for(Entry<AbstractConsumer,Mqtt> entry: map.entrySet()){
          logger.info("重新连接,重新订阅主题:{}",entry.getValue().topics());
          client.subscribe(entry.getValue().topics(),entry.getValue().qos(),entry.getKey());
        }
      }
    }
  }
}
