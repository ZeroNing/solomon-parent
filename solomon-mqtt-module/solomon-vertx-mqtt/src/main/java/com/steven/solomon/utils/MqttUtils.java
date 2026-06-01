package com.steven.solomon.utils;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.mqtt.AbstractMqttClientRegistry;
import com.steven.solomon.mqtt.MqttOperations;
import com.steven.solomon.mqtt.code.MqttErrorCodes;
import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mqtt.model.MqttSubscriptionDescriptor;
import com.steven.solomon.mqtt.support.MqttListenerRegistry;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.profile.MqttProfile.MqttWill;
import com.steven.solomon.service.SendService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;

/**
 * Vert.x MQTT 工具类。
 *
 * <p>基于 Vert.x MqttClient，提供消息发送、主题订阅/取消订阅、客户端连接管理、Vert.x 实例管理和连接参数构建等功能。</p>
 * <p>同时实现 {@link SendService} 和 {@link MqttOperations} 接口，统一消息发送入口。</p>
 */
@Configuration
public class MqttUtils extends AbstractMqttClientRegistry<MqttClient, MqttClientOptions>
    implements SendService<MqttMessageModel<?>>, MqttOperations {

  /** 日志记录器 */
  private final Logger logger = LoggerUtils.logger(MqttUtils.class);

  /** 租户与 Vert.x 实例的映射 */
  private final Map<String, Vertx> vertxMap = new ConcurrentHashMap<>();

  /** 租户与主题消费者的映射 */
  private final Map<String, Map<String, AbstractConsumer<?, ?>>> consumerMap = new ConcurrentHashMap<>();

  /**
   * 获取所有租户的 Vert.x 实例映射。
   *
   * @return 租户与 Vert.x 实例映射
   */
  public Map<String, Vertx> getVertxMap() {
    return vertxMap;
  }

  /**
   * 注册租户的 Vert.x 实例。
   *
   * @param tenantCode 租户编码
   * @param vertx Vert.x 实例
   */
  public void putVertx(String tenantCode, Vertx vertx) {
    vertxMap.put(tenantCode, vertx);
  }

  /**
   * 发送 MQTT 消息。
   *
   * @param data 消息模型，包含租户编码、主题和消息体
   */
  @Override
  public void send(MqttMessageModel<?> data) throws Exception {
    publishAsync(data, JSONUtil.toJsonStr(data));
  }

  /**
   * 异步发送 MQTT 消息，返回 CompletableFuture。
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
      logger.error("Vert.x MQTT 消息发送失败, tenant={}, topic={}, payload={}",
          data.getTenantCode(), data.getTopic(), json, e);
      CompletableFuture<Void> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
  }

  /**
   * 发送延时消息（Vert.x MQTT 不支持延时，直接发送）。
   *
   * @param data 消息模型
   * @param delay 延时时间（毫秒），此处忽略
   */
  @Override
  public void sendDelay(MqttMessageModel<?> data, long delay) throws Exception {
    send(data);
  }

  /**
   * 发送过期消息（Vert.x MQTT 不支持过期，直接发送）。
   *
   * @param data 消息模型
   * @param expiration 过期时间（毫秒），此处忽略
   */
  @Override
  public void sendExpiration(MqttMessageModel<?> data, long expiration) throws Exception {
    send(data);
  }

  /**
   * 订阅指定租户的 MQTT 主题。
   *
   * @param tenantCode 租户编码
   * @param topic 订阅主题
   * @param qos 消息质量等级
   * @param consumer 消费者实例
   */
  @Override
  public void subscribe(String tenantCode, String topic, int qos, Object consumer) throws Exception {
    if (ObjectUtil.isEmpty(topic)) {
      return;
    }
    MqttClient client = getClient(tenantCode);
    client.subscribe(topic, qos);
    // 保存主题与消费者的映射
    tenantConsumers(tenantCode).put(topic, (AbstractConsumer<?, ?>) consumer);
    // 注册消息发布处理器
    registerPublishHandler(client, tenantCode);
  }

  /**
   * 订阅指定租户的 MQTT 主题（AbstractConsumer 重载）。
   *
   * @param tenantCode 租户编码
   * @param topic 订阅主题
   * @param qos 消息质量等级
   * @param consumer 消费者实例
   */
  public void subscribe(String tenantCode, String topic, int qos, AbstractConsumer<?, ?> consumer)
      throws Exception {
    subscribe(tenantCode, topic, qos, (Object) consumer);
  }

  /**
   * 使用自动扫描的监听器订阅指定租户的所有主题。
   *
   * @param client MQTT 客户端
   * @param tenantCode 租户编码
   */
  public void subscribe(MqttClient client, String tenantCode) throws Exception {
    subscribe(client, SpringUtil.getBeanListWithAnnotation(
        com.steven.solomon.mqtt.annotation.MessageListener.class), tenantCode);
  }

  /**
   * 根据监听器列表解析订阅描述，逐一订阅主题。
   *
   * @param client MQTT 客户端
   * @param listenerList 监听器实例列表
   * @param tenantCode 租户编码
   */
  public void subscribe(MqttClient client, List<Object> listenerList, String tenantCode) {
    Map<String, AbstractConsumer<?, ?>> tenantConsumerMap = tenantConsumers(tenantCode);
    for (MqttSubscriptionDescriptor descriptor : MqttListenerRegistry.resolve(tenantCode, listenerList)) {
      client.subscribe(descriptor.getTopic(), descriptor.getQos());
      // 复制消费者实例，避免多租户共享同一实例导致并发问题
      tenantConsumerMap.put(descriptor.getTopic(), copyConsumer(descriptor.getListener()));
      logger.info("租户:{} 订阅 Vert.x MQTT 主题:{}", tenantCode, descriptor.getTopic());
    }
    registerPublishHandler(client, tenantCode);
  }

  /**
   * 取消订阅指定租户的主题。
   *
   * @param tenantCode 租户编码
   * @param topics 要取消订阅的主题数组
   */
  @Override
  public void unsubscribe(String tenantCode, String[] topics) throws Exception {
    if (ObjectUtil.isEmpty(topics)) {
      return;
    }
    MqttClient client = getClient(tenantCode);
    Map<String, AbstractConsumer<?, ?>> tenantConsumerMap = tenantConsumers(tenantCode);
    for (String topic : topics) {
      client.unsubscribe(topic);
      // 移除主题对应的消费者
      tenantConsumerMap.remove(topic);
    }
  }

  /**
   * 断开指定租户的 MQTT 连接，关闭 Vert.x 实例并清理所有资源。
   *
   * @param tenantCode 租户编码
   */
  @Override
  public void disconnect(String tenantCode) throws Exception {
    MqttClient client = getClient(tenantCode);
    client.disconnect();
    // 关闭并移除 Vert.x 实例
    Vertx vertx = vertxMap.remove(tenantCode);
    if (ObjectUtil.isNotEmpty(vertx)) {
      vertx.close();
    }
    // 清理消费者映射
    consumerMap.remove(tenantCode);
    // 从注册表中移除客户端
    removeClient(tenantCode);
  }

  /**
   * Vert.x 连接由初始化服务按原始配置重建。
   */
  @Override
  public void reconnect(String tenantCode) {
    logger.warn("Vert.x MQTT 需要通过初始化服务重建连接, tenant={}", tenantCode);
  }

  public void reconnect(String tenantCode, MqttProfile mqttProfile) {
    reconnect(tenantCode);
  }

  /**
   * 获取指定租户的 MQTT 客户端，不存在时抛出业务异常。
   *
   * @param tenantCode 租户编码
   * @return MQTT 客户端
   * @throws BaseException 客户端未初始化时抛出
   */
  private MqttClient getClient(String tenantCode) throws BaseException {
    return getRequiredClient(tenantCode, MqttErrorCodes.CLIENT_IS_NULL);
  }

  /**
   * 异步发布 MQTT 消息。
   *
   * @param data 消息模型
   * @param json 消息 JSON 字符串
   * @return 异步发送结果
   */
  private CompletableFuture<Void> publishAsync(MqttMessageModel<?> data, String json) throws Exception {
    CompletableFuture<Void> future = new CompletableFuture<>();
    getClient(data.getTenantCode()).publish(
        data.getTopic(),
        Buffer.buffer(json.getBytes(StandardCharsets.UTF_8)),
        MqttQoS.valueOf(data.getQos()),
        data.getRetained(),
        false).onComplete(result -> {
          if (result.succeeded()) {
            future.complete(null);
            return;
          }
          logger.error("Vert.x MQTT 异步发送失败, tenant={}, topic={}, payload={}",
              data.getTenantCode(), data.getTopic(), json, result.cause());
          future.completeExceptionally(result.cause());
        });
    return future;
  }

  /**
   * 根据 MqttProfile 构建 Vert.x MqttClientOptions。
   *
   * @param mqttProfile MQTT 客户端配置
   * @return Vert.x MQTT 客户端连接参数
   */
  public MqttClientOptions initMqttConnectOptions(MqttProfile mqttProfile) {
    MqttClientOptions options = new MqttClientOptions();
    options.setUsername(mqttProfile.getUserName());
    options.setPassword(mqttProfile.getPassword());
    options.setCleanSession(mqttProfile.isCleanSession());
    options.setAutoGeneratedClientId(false);
    options.setKeepAliveInterval(mqttProfile.getKeepAliveInterval());
    options.setAutoKeepAlive(true);
    options.setMaxInflightQueue(mqttProfile.getMaxInflight());
    options.setAckTimeout(mqttProfile.getCompletionTimeout() / 1000);
    options.setAutoAck(true);
    options.setReconnectAttempts(mqttProfile.getReconnectAttempts());
    options.setReconnectInterval(mqttProfile.getReconnectInterval());

    // 设置遗嘱消息
    MqttWill will = mqttProfile.getWill();
    if (ObjectUtil.isNotEmpty(will)) {
      options.setWillFlag(true);
      options.setWillTopic(will.getTopic());
      if (ObjectUtil.isNotEmpty(will.getMessage())) {
        options.setWillMessageBytes(Buffer.buffer(will.getMessage().getBytes(StandardCharsets.UTF_8)));
      }
      options.setWillQoS(will.getQos());
      options.setWillRetain(will.getRetained());
    }
    // 判断是否使用 SSL
    if (StrUtil.startWith(mqttProfile.getUrl(), "ssl://")) {
      options.setSsl(true);
    }
    // 关闭证书校验时信任所有证书
    if (!mqttProfile.isVerifyCertificate()) {
      options.setTrustAll(true);
    }
    return options;
  }

  /**
   * 根据 VertxConfig 创建 Vert.x 实例。
   *
   * @param vertxConfig Vert.x 配置，为空时使用默认配置
   * @return Vert.x 实例
   */
  public Vertx initVertx(MqttProfile.VertxConfig vertxConfig) {
    if (ObjectUtil.isEmpty(vertxConfig)) {
      return Vertx.vertx();
    }
    VertxOptions options = new VertxOptions()
        .setEventLoopPoolSize(vertxConfig.getEventLoopPoolSize())
        .setWorkerPoolSize(vertxConfig.getWorkerPoolSize())
        .setInternalBlockingPoolSize(vertxConfig.getInternalBlockingPoolSize())
        .setBlockedThreadCheckIntervalUnit(vertxConfig.getBlockedThreadCheckIntervalUnit())
        .setMaxEventLoopExecuteTimeUnit(vertxConfig.getMaxEventLoopExecuteTimeUnit())
        .setMaxWorkerExecuteTimeUnit(vertxConfig.getMaxWorkerExecuteTimeUnit())
        .setWarningExceptionTimeUnit(vertxConfig.getWarningExceptionTimeUnit())
        .setHAEnabled(vertxConfig.isHaEnabled())
        .setQuorumSize(vertxConfig.getQuorumSize())
        .setHAGroup(vertxConfig.getHaGroup())
        .setPreferNativeTransport(vertxConfig.isPreferNativeTransport())
        .setDisableTCCL(vertxConfig.isDisableTCCL());
    if (ObjectUtil.isNotEmpty(vertxConfig.getUseDaemonThread())) {
      options.setUseDaemonThread(vertxConfig.getUseDaemonThread());
    }
    return Vertx.vertx(options);
  }

  /**
   * 注册消息发布处理器，根据主题通配符匹配分发到对应的消费者。
   *
   * @param client MQTT 客户端
   * @param tenantCode 租户编码
   */
  private void registerPublishHandler(MqttClient client, String tenantCode) {
    client.publishHandler(message -> {
      try {
        Map<String, AbstractConsumer<?, ?>> tenantConsumerMap = tenantConsumers(tenantCode);
        // 使用通配符匹配找到对应的消费者
        String filter = MqttTopicFilterMatcher.findFirstMatchingFilter(
            message.topicName(), new ArrayList<>(tenantConsumerMap.keySet()));
        AbstractConsumer<?, ?> consumer = tenantConsumerMap.get(filter);
        if (ObjectUtil.isEmpty(consumer)) {
          logger.warn("租户:{} 未找到 MQTT 主题消费者, topic={}", tenantCode, message.topicName());
          return;
        }
        consumer.messageArrived(message.topicName(), message);
      } catch (Exception e) {
        logger.error("租户:{} 消费 Vert.x MQTT 消息失败, topic={}", tenantCode, message.topicName(), e);
      }
    });
  }

  /**
   * 获取或创建指定租户的消费者映射。
   *
   * @param tenantCode 租户编码
   * @return 主题与消费者映射
   */
  private Map<String, AbstractConsumer<?, ?>> tenantConsumers(String tenantCode) {
    return consumerMap.computeIfAbsent(tenantCode, key -> new ConcurrentHashMap<>());
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
