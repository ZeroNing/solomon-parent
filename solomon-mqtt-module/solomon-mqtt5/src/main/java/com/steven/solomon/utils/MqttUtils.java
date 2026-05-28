package com.steven.solomon.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.mqtt.AbstractMqttClientRegistry;
import com.steven.solomon.mqtt.MqttOperations;
import com.steven.solomon.mqtt.code.MqttErrorCodes;
import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mqtt.model.MqttSubscriptionDescriptor;
import com.steven.solomon.mqtt.support.MqttListenerRegistry;
import com.steven.solomon.mqtt.support.MqttSslFactory;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.profile.MqttProfile.MqttWill;
import com.steven.solomon.service.SendService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT5 工具类。
 *
 * <p>基于 Eclipse Paho MQTTv5 MqttAsyncClient，提供消息发送、主题订阅/取消订阅、客户端连接管理和连接参数构建等功能。</p>
 * <p>同时实现 {@link SendService} 和 {@link MqttOperations} 接口，统一消息发送入口。</p>
 */
@Configuration
public class MqttUtils extends AbstractMqttClientRegistry<MqttAsyncClient, MqttConnectionOptions>
    implements SendService<MqttMessageModel<?>>, MqttOperations {

  /** 日志记录器 */
  private final Logger logger = LoggerUtils.logger(MqttUtils.class);

  /**
   * 发送 MQTT5 消息。
   *
   * <p>底层使用 Paho MqttAsyncClient，方法只负责提交发送请求，不等待 Broker ACK。</p>
   *
   * @param data 消息模型，包含租户编码、主题和消息体
   */
  @Override
  public void send(MqttMessageModel<?> data) throws Exception {
    publishAsync(data, JSONUtil.toJsonStr(data));
  }

  /**
   * 异步发送 MQTT5 消息，返回 CompletableFuture。
   *
   * @param data 消息模型
   * @return 异步发送结果
   */
  @Override
  public CompletableFuture<Void> sendAsync(MqttMessageModel<?> data) {
    String json = JSONUtil.toJsonStr(data);
    try {
      return publishAsync(data, json);
    } catch (Exception e) {
      logger.error("MQTT5 消息发送失败, tenant={}, topic={}, payload={}",
          data.getTenantCode(), data.getTopic(), json, e);
      CompletableFuture<Void> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
  }

  /**
   * 发送延时消息（MQTT5 不支持延时，直接发送）。
   *
   * @param data 消息模型
   * @param delay 延时时间（毫秒），此处忽略
   */
  @Override
  public void sendDelay(MqttMessageModel<?> data, long delay) throws Exception {
    send(data);
  }

  /**
   * 发送过期消息（MQTT5 不支持过期，直接发送）。
   *
   * @param data 消息模型
   * @param expiration 过期时间（毫秒），此处忽略
   */
  @Override
  public void sendExpiration(MqttMessageModel<?> data, long expiration) throws Exception {
    send(data);
  }

  /**
   * 订阅指定租户的 MQTT5 主题。
   *
   * @param tenantCode 租户编码
   * @param topic 订阅主题
   * @param qos 消息质量等级
   * @param consumer 消费者实例
   */
  @Override
  public void subscribe(String tenantCode, String topic, int qos, Object consumer)
      throws MqttException, BaseException {
    if (ValidateUtils.isEmpty(topic)) {
      return;
    }
    getClient(tenantCode).subscribe(
        new MqttSubscription[]{new MqttSubscription(topic, qos)},
        (IMqttMessageListener) consumer);
  }

  /**
   * 订阅指定租户的 MQTT5 主题（IMqttMessageListener 重载）。
   *
   * @param tenantCode 租户编码
   * @param topic 订阅主题
   * @param qos 消息质量等级
   * @param consumer 消息监听器
   */
  public void subscribe(String tenantCode, String topic, int qos, IMqttMessageListener consumer)
      throws MqttException, BaseException {
    subscribe(tenantCode, topic, qos, (Object) consumer);
  }

  /**
   * 使用自动扫描的监听器订阅指定租户的所有主题。
   *
   * @param client MQTT5 异步客户端
   * @param tenantCode 租户编码
   */
  public void subscribe(MqttAsyncClient client, String tenantCode) throws MqttException {
    subscribe(client, SpringUtil.getBeanListWithAnnotation(
        com.steven.solomon.mqtt.annotation.MessageListener.class), tenantCode);
  }

  /**
   * 根据监听器列表解析订阅描述，逐一订阅主题。
   *
   * @param client MQTT5 异步客户端
   * @param listenerList 监听器实例列表
   * @param tenantCode 租户编码
   */
  public void subscribe(MqttAsyncClient client, List<Object> listenerList, String tenantCode)
      throws MqttException {
    for (MqttSubscriptionDescriptor descriptor : MqttListenerRegistry.resolve(tenantCode, listenerList)) {
      // 复制消费者实例，避免多租户共享同一实例导致并发问题
      AbstractConsumer<?, ?> consumer = copyConsumer(descriptor.getListener());
      client.subscribe(
          new MqttSubscription[]{new MqttSubscription(descriptor.getTopic(), descriptor.getQos())},
          consumer);
      logger.info("租户:{} 订阅 MQTT5 主题:{}", tenantCode, descriptor.getTopic());
    }
  }

  /**
   * 取消订阅指定租户的主题。
   *
   * @param tenantCode 租户编码
   * @param topics 要取消订阅的主题数组
   */
  @Override
  public void unsubscribe(String tenantCode, String[] topics) throws MqttException, BaseException {
    if (ValidateUtils.isEmpty(topics)) {
      return;
    }
    getClient(tenantCode).unsubscribe(topics);
  }

  /**
   * 断开指定租户的 MQTT5 连接并移除注册。
   *
   * @param tenantCode 租户编码
   */
  @Override
  public void disconnect(String tenantCode) throws MqttException, BaseException {
    MqttAsyncClient client = getClient(tenantCode);
    if (client.isConnected()) {
      client.disconnect();
    }
    // 从注册表中移除客户端和连接参数
    removeClient(tenantCode);
  }

  /**
   * 重新连接指定租户的 MQTT5 客户端并恢复订阅。
   *
   * @param tenantCode 租户编码
   */
  @Override
  public void reconnect(String tenantCode) throws MqttException, BaseException {
    MqttAsyncClient client = getClient(tenantCode);
    if (!client.isConnected()) {
      client.connect(getOptions(tenantCode)).waitForCompletion();
      // 重连后恢复所有订阅
      subscribe(client, tenantCode);
    }
  }

  /**
   * 使用新的配置重新连接指定租户的 MQTT5 客户端并恢复订阅。
   *
   * @param tenantCode 租户编码
   * @param profile 新的 MQTT5 客户端配置
   */
  public void reconnect(String tenantCode, MqttProfile profile) throws MqttException, BaseException {
    MqttAsyncClient client = getClient(tenantCode);
    if (!client.isConnected()) {
      client.connect(initMqttConnectOptions(profile)).waitForCompletion();
      subscribe(client, tenantCode);
    }
  }

  /**
   * 获取指定租户的 MQTT5 客户端，不存在时抛出业务异常。
   *
   * @param tenantCode 租户编码
   * @return MQTT5 异步客户端
   * @throws BaseException 客户端未初始化时抛出
   */
  private MqttAsyncClient getClient(String tenantCode) throws BaseException {
    return getRequiredClient(tenantCode, MqttErrorCodes.CLIENT_IS_NULL);
  }

  /**
   * 异步发布 MQTT5 消息。
   *
   * @param data 消息模型
   * @param json 消息 JSON 字符串
   * @return 异步发送结果
   */
  private CompletableFuture<Void> publishAsync(MqttMessageModel<?> data, String json) throws Exception {
    CompletableFuture<Void> future = new CompletableFuture<>();
    getClient(data.getTenantCode()).publish(
        data.getTopic(),
        json.getBytes(StandardCharsets.UTF_8),
        data.getQos(),
        data.getRetained(),
        null,
        new MqttActionListener() {
          @Override
          public void onSuccess(IMqttToken asyncActionToken) {
            future.complete(null);
          }

          @Override
          public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            logger.error("MQTT5 异步发送失败, tenant={}, topic={}, payload={}",
                data.getTenantCode(), data.getTopic(), json, exception);
            future.completeExceptionally(exception);
          }
        });
    return future;
  }

  /**
   * 根据 MqttProfile 构建 MQTT5 MqttConnectionOptions。
   *
   * @param mqttProfile MQTT5 客户端配置
   * @return Paho MQTT5 连接参数对象
   */
  public MqttConnectionOptions initMqttConnectOptions(MqttProfile mqttProfile) {
    MqttConnectionOptions options = new MqttConnectionOptions();
    options.setUserName(mqttProfile.getUserName());
    options.setPassword(mqttProfile.getPassword().getBytes(StandardCharsets.UTF_8));
    // 支持多地址集群
    options.setServerURIs(mqttProfile.getUrl().split(","));
    options.setReceiveMaximum(mqttProfile.getReceiveMaximum());
    options.setAutomaticReconnect(mqttProfile.getAutomaticReconnect());
    options.setCleanStart(mqttProfile.getCleanStart());
    options.setKeepAliveInterval(mqttProfile.getKeepAliveInterval());
    options.setMaxReconnectDelay(mqttProfile.getMaxReconnectDelay());
    options.setConnectionTimeout(mqttProfile.getConnectionTimeout());
    options.setExecutorServiceTimeout(mqttProfile.getExecutorServiceTimeout());
    options.setRequestProblemInfo(mqttProfile.getRequestProblemInformation());
    options.setRequestResponseInfo(mqttProfile.getRequestResponseInformation());
    options.setMaximumPacketSize(mqttProfile.getMaximumPacketSize());
    options.setAutomaticReconnectDelay(
        mqttProfile.getAutomaticReconnectMinDelay(),
        mqttProfile.getAutomaticReconnectMaxDelay());
    options.setSendReasonMessages(mqttProfile.getSendReasonMessages());
    options.setSessionExpiryInterval(mqttProfile.getSessionExpiryInterval());

    // 设置 MQTT5 专有属性
    if (mqttProfile.getTopicAliasMax() != null && mqttProfile.getTopicAliasMax() > 0) {
      options.setTopicAliasMaximum(mqttProfile.getTopicAliasMax());
    }

    if (mqttProfile.getUserProperties() != null && !mqttProfile.getUserProperties().isEmpty()) {
      UserProperty[] userProps = mqttProfile.getUserProperties().entrySet().stream()
              .map(entry -> new UserProperty(entry.getKey(), entry.getValue()))
              .toArray(UserProperty[]::new);
      options.setUserProperties(List.of(userProps));
    }

    // 设置遗嘱消息
    MqttWill will = mqttProfile.getWill();
    if (ValidateUtils.isNotEmpty(will)) {
      MqttMessage message = new MqttMessage(will.getMessage().getBytes(StandardCharsets.UTF_8));
      message.setQos(will.getQos());
      message.setRetained(will.getRetained());
      options.setWill(will.getTopic(), message);
    }
    // 关闭证书校验时使用信任所有证书的 SocketFactory
    if (!mqttProfile.getVerifyCertificate()) {
      options.setSocketFactory(MqttSslFactory.trustAllSocketFactory());
    }
    return options;
  }

  /**
   * 复制消费者实例，为每个订阅创建独立的消费者对象。
   *
   * @param listener 原始监听器实例
   * @return 复制后的消费者实例
   */
  @SuppressWarnings("unchecked")
  private AbstractConsumer<?, ?> copyConsumer(Object listener) {
    return (AbstractConsumer<?, ?>) BeanUtil.copyProperties(listener, listener.getClass(), (String) null);
  }
}
