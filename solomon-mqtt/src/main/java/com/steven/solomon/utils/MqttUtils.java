package com.steven.solomon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.logger.LoggerUtils;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class MqttUtils {

  private Logger logger = LoggerUtils.logger(MqttUtils.class);

  @Resource
  private MqttClient client;

  /**
   *   发送消息
   *   @param topic 主题
   *   @param data 消息内容
   */
  public void send(String topic, MqttModel data) {
    // 获取客户端实例
    ObjectMapper mapper = new ObjectMapper();
    try {
      // 转换消息为json字符串
      String json = mapper.writeValueAsString(data);
      client.publish(topic, new MqttMessage(json.getBytes(StandardCharsets.UTF_8)));
    } catch (JsonProcessingException e) {
      logger.error(String.format("MQTT: 主题[%s]发送消息转换json失败", topic));
    } catch (MqttException e) {
      logger.error(String.format("MQTT: 主题[%s]发送消息失败", topic));
    }
  }
}
