package com.steven.solomon.service;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.lang.UUID;
import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
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

/**
 * MQTT5 默认客户端初始化服务。
 *
 * <p>基于 Eclipse Paho MQTTv5 MqttAsyncClient 实现，负责创建连接、注册重连回调并自动订阅监听器。</p>
 */
public class DefaultMqttInitService implements MqttClientInitService<MqttProfile> {

  /** 日志记录器 */
  private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

  /** MQTT5 工具类，负责连接参数构建、客户端注册与消息发送 */
  private final MqttUtils utils;

  /**
   * 构造方法。
   *
   * @param utils MQTT5 工具类实例
   */
  public DefaultMqttInitService(MqttUtils utils) {
    this.utils = utils;
  }

  /**
   * 初始化指定租户的 MQTT5 客户端（自动扫描监听器）。
   *
   * @param tenantCode 租户编码
   * @param mqttProfile MQTT5 客户端配置
   */
  @Override
  public void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception {
    initMqttClient(tenantCode, mqttProfile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
  }

  /**
   * 初始化指定租户的 MQTT5 客户端并订阅监听器。
   *
   * @param tenantCode 租户编码
   * @param mqttProfile MQTT5 客户端配置
   * @param listenerList 监听器实例列表
   */
  @Override
  public void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> listenerList)
    throws Exception {
    // 取第一个 URL 作为连接地址
    String url = mqttProfile.getUrl().split(",")[0];
    // 客户端 ID 为空时自动生成 UUID
    String clientId = ObjectUtil.defaultIfNull(mqttProfile.getClientId(), UUID.randomUUID().toString());
    MqttAsyncClient client = new MqttAsyncClient(url, clientId);
    MqttConnectionOptions options = utils.initMqttConnectOptions(mqttProfile);
    client.setCallback(callback(tenantCode, client, listenerList));
    // 初始化阶段等待连接完成，业务发送使用 MqttAsyncClient 异步 publish
    client.connect(options).waitForCompletion();
    utils.putOptionsMap(tenantCode, options);
    utils.putClient(tenantCode, client);
    // 订阅所有匹配的监听器
    utils.subscribe(client, listenerList, tenantCode);
    logger.info("租户:{} MQTT5 客户端初始化成功, clientId={}", tenantCode, clientId);
  }

  /**
   * 创建 MQTT5 连接回调，处理重连后恢复订阅和连接断开日志。
   *
   * @param tenantCode 租户编码
   * @param client MQTT5 异步客户端
   * @param listenerList 监听器实例列表
   * @return MQTT5 回调实例
   */
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
        // 消息由 IMqttMessageListener 单独处理，此处无需额外逻辑
      }

      @Override
      public void deliveryComplete(IMqttToken token) {
        // 发送完成回调，当前无需特殊处理
      }

      @Override
      public void connectComplete(boolean reconnect, String serverURI) {
        logger.info("租户:{} MQTT5 连接完成, reconnect={}, server={}", tenantCode, reconnect, serverURI);
        // 重连后自动恢复订阅
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
        // 认证包回调，当前无需特殊处理
      }
    };
  }
}
