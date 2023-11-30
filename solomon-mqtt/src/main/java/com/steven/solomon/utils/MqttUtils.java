package com.steven.solomon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.annotation.Mqtt;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.service.SendService;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MqttUtils implements SendService<MqttModel> {

  private Logger logger = LoggerUtils.logger(MqttUtils.class);

  private Map<String,MqttClient> clientMap = new HashMap<>();

  private Map<String,MqttConnectOptions> optionsMap = new HashMap<>();

  public Map<String, MqttConnectOptions> getOptionsMap() {
    return optionsMap;
  }

  public void putOptionsMap(String tenantCode, MqttConnectOptions options) {
    this.optionsMap.put(tenantCode,options);
  }

  private Map<String,Map<AbstractConsumer,Mqtt>> tenantAbstractConsumerMap = new HashMap<>();

  public Map<String, MqttClient> getClientMap() {
    return clientMap;
  }

  public void putClient(String tenantCode,MqttClient client) {
    this.clientMap.put(tenantCode, client);
  }

  public Map<String, Map<AbstractConsumer, Mqtt>> getTenantAbstractConsumerMap() {
    return tenantAbstractConsumerMap;
  }

  public void putTenantAbstractConsumerMap(String tenantCode,Map<AbstractConsumer, Mqtt> tenantAbstractConsumerMap) {
    this.tenantAbstractConsumerMap.put(tenantCode, tenantAbstractConsumerMap);
  }

  /**
   *   发送消息
   *   @param data 消息内容
   */
  @Override
  public void send(MqttModel data) {
    // 获取客户端实例
    ObjectMapper mapper = new ObjectMapper();
    try {
      // 转换消息为json字符串
      String json = mapper.writeValueAsString(data);
      getClientMap().get(data.getTenantCode()).getTopic(data.getTopic()).publish(new MqttMessage(json.getBytes(StandardCharsets.UTF_8)));
    } catch (JsonProcessingException e) {
      logger.error(String.format("MQTT: 主题[%s]发送消息转换json失败", data.getTopic()));
    } catch (MqttException e) {
      logger.error(String.format("MQTT: 主题[%s]发送消息失败", data.getTopic()));
    }
  }

  @Override
  public void sendDelay(MqttModel data, long delay) throws Exception {
    send(data);
  }

  @Override
  public void sendExpiration(MqttModel data, long expiration) throws Exception {
    send(data);
  }

  /**
   * 订阅消息
   * @param tenantCode 租户编码
   * @param topic 主题
   * @param qos 消息质量
   * @param consumer 消费者
   */
  public void subscribe(String tenantCode,String topic,int qos, AbstractConsumer consumer) throws MqttException {
    if(ValidateUtils.isEmpty(topic)){
      return;
    }
    getClientMap().get(tenantCode).subscribe(topic, qos,consumer);
  }

  /**
   * 取消订阅
   * @param topic 主题
   */
  public void unsubscribe(String tenantCode,String[] topic) throws MqttException {
    if(ValidateUtils.isEmpty(topic)){
      return;
    }
    getClientMap().get(tenantCode).unsubscribe(topic);
  }

  /**
   * 关闭连接
   */
  public void disconnect(String tenantCode) throws MqttException {
    getClientMap().get(tenantCode).disconnect();
  }

  /**
   * 重新连接
   */
  public void reconnect(String tenantCode) throws MqttException {
    MqttClient client = getClientMap().get(tenantCode);
    if(!client.isConnected()){
      client.connect(getOptionsMap().get(tenantCode));
      Map<AbstractConsumer,Mqtt> consumer = getTenantAbstractConsumerMap().get(tenantCode);
      for(Entry<AbstractConsumer,Mqtt> map : consumer.entrySet()){
        Mqtt mqtt = map.getValue();
        logger.debug("重新连接,重新订阅主题:{}",mqtt.topics());
        for(String topic : mqtt.topics()){
          client.subscribe(topic,mqtt.qos(),map.getKey());
        }
      }
    }
  }
}
