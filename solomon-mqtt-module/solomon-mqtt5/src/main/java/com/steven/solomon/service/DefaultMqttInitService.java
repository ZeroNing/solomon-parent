package com.steven.solomon.service;

import cn.hutool.core.lang.UUID;
import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.List;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;

public class DefaultMqttInitService implements MqttClientInitService<MqttProfile> {

  private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

  private final MqttUtils utils;

  public DefaultMqttInitService(MqttUtils utils) {
    this.utils = utils;
  }

  @Override
  public void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception {
    initMqttClient(tenantCode, mqttProfile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
  }

  @Override
  public void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> listenerList)
    throws Exception {
    String url = mqttProfile.getUrl().split(",")[0];
    String clientId = ValidateUtils.getOrDefault(mqttProfile.getClientId(), UUID.randomUUID().toString());
    MqttAsyncClient client = new MqttAsyncClient(url, clientId);
    MqttConnectionOptions options = utils.initMqttConnectOptions(mqttProfile);
    client.setCallback(callback(tenantCode, client, listenerList));
    // 中文注释：初始化阶段等待连接完成，业务发送使用 MqttAsyncClient 异步 publish。
    client.connect(options).waitForCompletion();
    utils.putOptionsMap(tenantCode, options);
    utils.putClient(tenantCode, client);
    utils.subscribe(client, listenerList, tenantCode);
    logger.info("租户:{} MQTT5 客户端初始化成功, clientId={}", tenantCode, clientId);
  }

  private MqttCallback callback(String tenantCode, MqttAsyncClient client, List<Object> listenerList) {
    return new MqttCallback() {
      @Override
      public void disconnected(MqttDisconnectResponse response) {
        logger.warn("租户:{} MQTT5 连接断开", tenantCode, response.getException());
      }

      @Override
      public void mqttErrorOccurred(MqttException e) {
        logger.error("租户:{} MQTT5 运行错误", tenantCode, e);
      }

      @Override
      public void messageArrived(String topic, MqttMessage message) {
      }

      @Override
      public void deliveryComplete(IMqttToken token) {
      }

      @Override
      public void connectComplete(boolean reconnect, String serverURI) {
        logger.info("租户:{} MQTT5 连接完成, reconnect={}, server={}", tenantCode, reconnect, serverURI);
        if (reconnect) {
          try {
            utils.subscribe(client, listenerList, tenantCode);
          } catch (Exception e) {
            logger.error("租户:{} MQTT5 重连恢复订阅失败", tenantCode, e);
          }
        }
      }

      @Override
      public void authPacketArrived(int reasonCode, MqttProperties properties) {
      }
    };
  }
}
