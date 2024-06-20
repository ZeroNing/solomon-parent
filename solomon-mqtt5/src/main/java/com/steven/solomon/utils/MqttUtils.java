package com.steven.solomon.utils;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.annotation.Mqtt;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.profile.MqttProfile.MqttWill;
import com.steven.solomon.service.SendService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class MqttUtils implements SendService<MqttModel> {

  private Logger logger = LoggerUtils.logger(MqttUtils.class);

  private Map<String, MqttClient> clientMap = new HashMap<>();

  private Map<String,MqttConnectionOptions> optionsMap = new HashMap<>();

  public Map<String, MqttConnectionOptions> getOptionsMap() {
    return optionsMap;
  }

  public void putOptionsMap(String tenantCode, MqttConnectionOptions options) {
    this.optionsMap.put(tenantCode,options);
  }

  public Map<String, MqttClient> getClientMap() {
    return clientMap;
  }

  public void putClient(String tenantCode,MqttClient client) {
    this.clientMap.put(tenantCode, client);
  }

  /**
   *   发送消息
   *   @param data 消息内容
   */
  @Override
  public void send(MqttModel data) throws Exception {
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
  public void subscribe(String tenantCode,String topic,int qos, IMqttMessageListener consumer) throws MqttException {
    if(ValidateUtils.isEmpty(topic)){
      return;
    }
    getClientMap().get(tenantCode).subscribe(topic, qos,consumer);
  }

  /**
   * 订阅消息
   * @param client mqtt连接
   */
  public void subscribe(MqttClient client) throws MqttException {
    List<Object> clazzList = new ArrayList<>(SpringUtil.getBeansWithAnnotation(Mqtt.class).values());
    if (ValidateUtils.isNotEmpty(clazzList)) {
      for (Object abstractConsumer : clazzList) {
        Mqtt mqtt = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), Mqtt.class);
        if (ValidateUtils.isEmpty(mqtt)) {
          continue;
        }
        for (String topic : mqtt.topics()) {
          client.subscribe(topic, mqtt.qos(), (IMqttMessageListener) BeanUtil.copyProperties(abstractConsumer,abstractConsumer.getClass(), (String) null));
        }
      }
    }
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
      subscribe(client);
    }
  }

  public MqttConnectionOptions initMqttConnectOptions(MqttProfile mqttProfile) {
    MqttConnectionOptions mqttConnectOptions = new MqttConnectionOptions();
    mqttConnectOptions.setUserName(mqttProfile.getUserName());
    mqttConnectOptions.setPassword(mqttProfile.getPassword().getBytes());
    mqttConnectOptions.setServerURIs(new String[]{mqttProfile.getUrl()});
    //客户端愿意接收的 QoS 1 和 QoS 2 消息的最大数量
    mqttConnectOptions.setReceiveMaximum(mqttProfile.getReceiveMaximum());
    //设置超时时间
    mqttConnectOptions.setConnectionTimeout(mqttProfile.getCompletionTimeout());
    //设置自动重连
    mqttConnectOptions.setAutomaticReconnect(mqttProfile.getAutomaticReconnect());
    //客户端是否应该在连接时重置会话状态。如果设置为 true，客户端将不会恢复之前的会话状态，而是开始一个新的会话。如果设置为 false，客户端将尝试恢复之前的会话状态。
    mqttConnectOptions.setCleanStart(mqttProfile.getCleanStart());
    // 设置会话心跳时间 单位为秒   设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线，但这个方法并没有重连的机制
    mqttConnectOptions.setKeepAliveInterval(mqttProfile.getKeepAliveInterval());
    // 设置重新连接之间等待的最长时间
    mqttConnectOptions.setMaxReconnectDelay(mqttProfile.getMaxReconnectDelay());
    // 设置连接超时值,该值以秒为单位 0 禁用超时处理,这意味着客户端将等待，直到网络连接成功或失败.
    mqttConnectOptions.setConnectionTimeout(mqttProfile.getConnectionTimeout());
    // 设置执行器服务应等待的时间（以秒为单位）在强制终止之前终止.不建议更改除非您绝对确定需要，否则该值.
    mqttConnectOptions.setExecutorServiceTimeout(mqttProfile.getExecutorServiceTimeout());
    // 指示客户端是否请求服务器在发生错误时发送问题信息
    mqttConnectOptions.setRequestProblemInfo(mqttProfile.getRequestProblemInformation());
    // 指示客户端是否请求服务器发送响应信息
    mqttConnectOptions.setRequestResponseInfo(mqttProfile.getRequestResponseInformation());
    // 客户端愿意接收的最大 MQTT 数据包大小。
    mqttConnectOptions.setMaximumPacketSize(mqttProfile.getMaximumPacketSize());
    // 自动重新连接尝试之间的最小和最大延迟时间(以秒为单位）
    mqttConnectOptions.setAutomaticReconnectDelay(mqttProfile.getAutomaticReconnectMinDelay(),mqttProfile.getAutomaticReconnectMinDelay());
    // 指示是否发送原因码消息
    mqttConnectOptions.setSendReasonMessages(mqttProfile.getSendReasonMessages());
    // 会话过期间隔时间（以秒为单位），如果设置为 null，则使用默认值（通常是无限期）
    mqttConnectOptions.setSessionExpiryInterval(mqttProfile.getSessionExpiryInterval());
    //设置遗嘱消息
    if (ValidateUtils.isNotEmpty(mqttProfile.getWill())) {
      MqttWill will = mqttProfile.getWill();
      MqttMessage message = new MqttMessage(will.getMessage().getBytes(StandardCharsets.UTF_8));
      message.setQos(will.getQos());
      message.setRetained(will.getRetained());
      mqttConnectOptions.setWill(will.getTopic(), message);
    }
    return mqttConnectOptions;
  }
}