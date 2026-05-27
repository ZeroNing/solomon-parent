package com.steven.solomon.utils;

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
import com.steven.solomon.verification.ValidateUtils;
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
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttUtils extends AbstractMqttClientRegistry<MqttClient, MqttClientOptions>
    implements SendService<MqttMessageModel<?>>, MqttOperations {

  private final Logger logger = LoggerUtils.logger(MqttUtils.class);

  private final Map<String, Vertx> vertxMap = new ConcurrentHashMap<>();

  private final Map<String, Map<String, AbstractConsumer<?, ?>>> consumerMap = new ConcurrentHashMap<>();

  public Map<String, Vertx> getVertxMap() {
    return vertxMap;
  }

  public void putVertx(String tenantCode, Vertx vertx) {
    vertxMap.put(tenantCode, vertx);
  }

  @Override
  public void send(MqttMessageModel<?> data) throws Exception {
    String json = JSONUtil.toJsonStr(data);
    try {
      getClient(data.getTenantCode()).publish(
          data.getTopic(),
          Buffer.buffer(json.getBytes(StandardCharsets.UTF_8)),
          MqttQoS.valueOf(data.getQos()),
          data.getRetained(),
          false);
    } catch (Exception e) {
      logger.error("Vert.x MQTT 消息发送失败, tenant={}, topic={}, payload={}",
          data.getTenantCode(), data.getTopic(), json, e);
      throw e;
    }
  }

  @Override
  public void sendDelay(MqttMessageModel<?> data, long delay) throws Exception {
    send(data);
  }

  @Override
  public void sendExpiration(MqttMessageModel<?> data, long expiration) throws Exception {
    send(data);
  }

  @Override
  public void subscribe(String tenantCode, String topic, int qos, Object consumer) throws Exception {
    if (ValidateUtils.isEmpty(topic)) {
      return;
    }
    MqttClient client = getClient(tenantCode);
    client.subscribe(topic, qos);
    tenantConsumers(tenantCode).put(topic, (AbstractConsumer<?, ?>) consumer);
    registerPublishHandler(client, tenantCode);
  }

  public void subscribe(String tenantCode, String topic, int qos, AbstractConsumer<?, ?> consumer)
      throws Exception {
    subscribe(tenantCode, topic, qos, (Object) consumer);
  }

  public void subscribe(MqttClient client, String tenantCode) throws Exception {
    subscribe(client, SpringUtil.getBeanListWithAnnotation(
        com.steven.solomon.mqtt.annotation.MessageListener.class), tenantCode);
  }

  public void subscribe(MqttClient client, List<Object> listenerList, String tenantCode) {
    Map<String, AbstractConsumer<?, ?>> tenantConsumerMap = tenantConsumers(tenantCode);
    for (MqttSubscriptionDescriptor descriptor : MqttListenerRegistry.resolve(tenantCode, listenerList)) {
      client.subscribe(descriptor.getTopic(), descriptor.getQos());
      tenantConsumerMap.put(descriptor.getTopic(), copyConsumer(descriptor.getListener()));
      logger.info("租户:{} 订阅 Vert.x MQTT 主题:{}", tenantCode, descriptor.getTopic());
    }
    registerPublishHandler(client, tenantCode);
  }

  @Override
  public void unsubscribe(String tenantCode, String[] topics) throws Exception {
    if (ValidateUtils.isEmpty(topics)) {
      return;
    }
    MqttClient client = getClient(tenantCode);
    Map<String, AbstractConsumer<?, ?>> tenantConsumerMap = tenantConsumers(tenantCode);
    for (String topic : topics) {
      client.unsubscribe(topic);
      tenantConsumerMap.remove(topic);
    }
  }

  @Override
  public void disconnect(String tenantCode) throws Exception {
    MqttClient client = getClient(tenantCode);
    client.disconnect();
    Vertx vertx = vertxMap.remove(tenantCode);
    if (ValidateUtils.isNotEmpty(vertx)) {
      vertx.close();
    }
    consumerMap.remove(tenantCode);
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

  private MqttClient getClient(String tenantCode) throws BaseException {
    return getRequiredClient(tenantCode, MqttErrorCodes.CLIENT_IS_NULL);
  }

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

    MqttWill will = mqttProfile.getWill();
    if (ValidateUtils.isNotEmpty(will)) {
      options.setWillFlag(true);
      options.setWillTopic(will.getTopic());
      if (ValidateUtils.isNotEmpty(will.getMessage())) {
        options.setWillMessageBytes(Buffer.buffer(will.getMessage().getBytes(StandardCharsets.UTF_8)));
      }
      options.setWillQoS(will.getQos());
      options.setWillRetain(will.getRetained());
    }
    if (StrUtil.startWith(mqttProfile.getUrl(), "ssl://")) {
      options.setSsl(true);
    }
    if (!mqttProfile.isVerifyCertificate()) {
      options.setTrustAll(true);
    }
    return options;
  }

  public Vertx initVertx(MqttProfile.VertxConfig vertxConfig) {
    if (ValidateUtils.isEmpty(vertxConfig)) {
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
    if (ValidateUtils.isNotEmpty(vertxConfig.getUseDaemonThread())) {
      options.setUseDaemonThread(vertxConfig.getUseDaemonThread());
    }
    return Vertx.vertx(options);
  }

  private void registerPublishHandler(MqttClient client, String tenantCode) {
    client.publishHandler(message -> {
      try {
        Map<String, AbstractConsumer<?, ?>> tenantConsumerMap = tenantConsumers(tenantCode);
        String filter = MqttTopicFilterMatcher.findFirstMatchingFilter(
            message.topicName(), new ArrayList<>(tenantConsumerMap.keySet()));
        AbstractConsumer<?, ?> consumer = tenantConsumerMap.get(filter);
        if (ValidateUtils.isEmpty(consumer)) {
          logger.warn("租户:{} 未找到 MQTT 主题消费者, topic={}", tenantCode, message.topicName());
          return;
        }
        consumer.messageArrived(message.topicName(), message);
      } catch (Exception e) {
        logger.error("租户:{} 消费 Vert.x MQTT 消息失败, topic={}", tenantCode, message.topicName(), e);
      }
    });
  }

  private Map<String, AbstractConsumer<?, ?>> tenantConsumers(String tenantCode) {
    return consumerMap.computeIfAbsent(tenantCode, key -> new ConcurrentHashMap<>());
  }

  @SuppressWarnings("unchecked")
  private AbstractConsumer<?, ?> copyConsumer(Object listener) {
    return (AbstractConsumer<?, ?>) BeanUtil.copyProperties(listener, listener.getClass(), (String) null);
  }
}
