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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;

public class DefaultMqttInitService implements MqttClientInitService<MqttProfile> {

  private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

  private final MqttUtils utils;

  public DefaultMqttInitService(MqttUtils utils) {
    this.utils = utils;
  }

  @Override
  public void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> listenerList)
      throws Exception {
    String url = mqttProfile.getUrl().split(",")[0];
    String clientId = ValidateUtils.getOrDefault(mqttProfile.getClientId(), UUID.randomUUID().toString());
    MqttClient mqttClient = new MqttClient(url, clientId);
    MqttConnectOptions options = utils.initMqttConnectOptions(mqttProfile);
    mqttClient.setCallback(callback(tenantCode, mqttClient, listenerList));
    mqttClient.connect(options);
    utils.putOptionsMap(tenantCode, options);
    utils.putClient(tenantCode, mqttClient);
    utils.subscribe(mqttClient, listenerList, tenantCode);
    logger.info("租户:{} MQTT3 客户端初始化成功, clientId={}", tenantCode, clientId);
  }

  @Override
  public void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception {
    initMqttClient(tenantCode, mqttProfile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
  }

  private MqttCallbackExtended callback(String tenantCode, MqttClient client, List<Object> listenerList) {
    return new MqttCallbackExtended() {
      @Override
      public void connectComplete(boolean reconnect, String serverURI) {
        logger.info("租户:{} MQTT3 连接完成, reconnect={}, server={}", tenantCode, reconnect, serverURI);
        if (reconnect) {
          try {
            utils.subscribe(client, listenerList, tenantCode);
          } catch (Exception e) {
            logger.error("租户:{} MQTT3 重连恢复订阅失败", tenantCode, e);
          }
        }
      }

      @Override
      public void connectionLost(Throwable cause) {
        logger.warn("租户:{} MQTT3 连接断开", tenantCode, cause);
      }

      @Override
      public void messageArrived(String topic, MqttMessage message) {
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {
      }
    };
  }
}
