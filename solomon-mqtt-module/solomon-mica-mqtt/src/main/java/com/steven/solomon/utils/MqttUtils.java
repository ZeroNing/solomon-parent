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
import com.steven.solomon.service.SendService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.codec.message.builder.MqttTopicSubscription;
import org.dromara.mica.mqtt.core.client.MqttClient;
import org.dromara.mica.mqtt.core.client.MqttClientCreator;
import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttUtils extends AbstractMqttClientRegistry<MqttClient, MqttClientProperties>
    implements SendService<MqttMessageModel<?>>, MqttOperations {

  private final Logger logger = LoggerUtils.logger(MqttUtils.class);

  @Override
  public void send(MqttMessageModel<?> data) throws Exception {
    String json = JSONUtil.toJsonStr(data);
    try {
      getClient(data.getTenantCode()).publish(
          data.getTopic(),
          json.getBytes(StandardCharsets.UTF_8),
          MqttQoS.valueOf(data.getQos()),
          data.getRetained());
    } catch (Exception e) {
      logger.error("Mica MQTT 消息发送失败, tenant={}, topic={}, payload={}",
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
  public void subscribe(String tenantCode, String topic, int qos, Object consumer) throws BaseException {
    if (ValidateUtils.isEmpty(topic)) {
      return;
    }
    getClient(tenantCode).subscribe(topic, MqttQoS.valueOf(qos), (AbstractConsumer<?, ?>) consumer);
  }

  public void subscribe(String tenantCode, String topic, int qos, AbstractConsumer<?, ?> consumer)
      throws BaseException {
    subscribe(tenantCode, topic, qos, (Object) consumer);
  }

  public void subscribe(MqttClient client, String tenantCode) {
    subscribe(client, SpringUtil.getBeanListWithAnnotation(
        com.steven.solomon.mqtt.annotation.MessageListener.class), tenantCode);
  }

  public void subscribe(MqttClient client, List<Object> listenerList, String tenantCode) {
    for (MqttSubscriptionDescriptor descriptor : MqttListenerRegistry.resolve(tenantCode, listenerList)) {
      client.subscribe(
          descriptor.getTopic(),
          MqttQoS.valueOf(descriptor.getQos()),
          copyConsumer(descriptor.getListener()));
      logger.info("租户:{} 订阅 Mica MQTT 主题:{}", tenantCode, descriptor.getTopic());
    }
  }

  @Override
  public void unsubscribe(String tenantCode, String[] topics) throws BaseException {
    if (ValidateUtils.isEmpty(topics)) {
      return;
    }
    MqttClient client = getClient(tenantCode);
    for (String topic : topics) {
      client.unSubscribe(topic);
    }
  }

  @Override
  public void disconnect(String tenantCode) throws BaseException {
    MqttClient client = getClient(tenantCode);
    if (ValidateUtils.isNotEmpty(client)) {
      client.disconnect();
    }
    removeClient(tenantCode);
  }

  @Override
  public void reconnect(String tenantCode) throws BaseException {
    MqttClient client = getClient(tenantCode);
    if (ValidateUtils.isNotEmpty(client) && !client.isConnected()) {
      client.reconnect();
      subscribe(client, tenantCode);
    }
  }

  public void reconnect(String tenantCode, MqttClientProperties mqttProfile) throws BaseException {
    putOptionsMap(tenantCode, mqttProfile);
    reconnect(tenantCode);
  }

  public MqttClient createMqttClient(String tenantCode, MqttClientProperties properties) {
    putOptionsMap(tenantCode, properties);
    MqttClientCreator clientCreator = MqttClient.create()
        .name(properties.getName())
        .ip(properties.getIp())
        .port(properties.getPort())
        .username(properties.getUsername())
        .password(properties.getPassword())
        .clientId(properties.getClientId())
        .bindIp(properties.getBindIp())
        .bindNetworkInterface(properties.getBindNetworkInterface())
        .readBufferSize((int) properties.getReadBufferSize().toBytes())
        .maxBytesInMessage((int) properties.getMaxBytesInMessage().toBytes())
        .maxClientIdLength(properties.getMaxClientIdLength())
        .keepAliveSecs(properties.getKeepAliveSecs())
        .heartbeatMode(properties.getHeartbeatMode())
        .heartbeatTimeoutStrategy(properties.getHeartbeatTimeoutStrategy())
        .reconnect(properties.isReconnect())
        .reInterval(properties.getReInterval())
        .retryCount(properties.getRetryCount())
        .reSubscribeBatchSize(properties.getReSubscribeBatchSize())
        .version(properties.getVersion())
        .cleanStart(properties.isCleanStart())
        .sessionExpiryIntervalSecs(properties.getSessionExpiryIntervalSecs())
        .statEnable(properties.isStatEnable())
        .debug(properties.isDebug())
        .disconnectBeforeStop(properties.isDisconnectBeforeStop());
    if (ValidateUtils.isNotEmpty(properties.getTimeout()) && properties.getTimeout() > 0) {
      clientCreator.timeout(properties.getTimeout());
    }
    if (ValidateUtils.isNotEmpty(properties.getBizThreadPoolSize())
        && properties.getBizThreadPoolSize() > 0) {
      clientCreator.bizThreadPoolSize(properties.getBizThreadPoolSize());
    }
    applySsl(clientCreator, properties);
    applyWill(clientCreator, properties);
    List<MqttTopicSubscription> globalSubscribe = properties.getGlobalSubscribe();
    if (ValidateUtils.isNotEmpty(globalSubscribe)) {
      clientCreator.globalSubscribe(globalSubscribe);
    }
    MqttClient client = clientCreator.connect();
    putClient(tenantCode, client);
    return client;
  }

  private MqttClient getClient(String tenantCode) throws BaseException {
    return getRequiredClient(tenantCode, MqttErrorCodes.MQTT_CLIENT_IS_NULL);
  }

  private void applyWill(MqttClientCreator clientCreator, MqttClientProperties properties) {
    MqttClientProperties.WillMessage willMessage = properties.getWillMessage();
    if (ValidateUtils.isEmpty(willMessage) || StrUtil.isBlank(willMessage.getTopic())) {
      return;
    }
    clientCreator.willMessage(builder -> {
      builder.topic(willMessage.getTopic())
          .qos(willMessage.getQos())
          .retain(willMessage.isRetain());
      if (StrUtil.isNotBlank(willMessage.getMessage())) {
        builder.messageText(willMessage.getMessage());
      }
    });
  }

  /**
   * 兼容 Mica-MQTT 不同版本的 SSL API。
   */
  private void applySsl(MqttClientCreator clientCreator, MqttClientProperties properties) {
    Object ssl = invokeNoArg(properties, "getSsl");
    boolean sslEnabled = isSslEnabled(ssl) || properties.getPort() == 8883;
    if (!sslEnabled) {
      return;
    }
    invokeNoArg(clientCreator, "useSsl");
    Object sslConfig = buildSslConfig(ssl);
    if (ValidateUtils.isNotEmpty(sslConfig)) {
      invokeOneArg(clientCreator, "sslConfig", sslConfig);
    }
    logger.info("Mica MQTT SSL 已启用, host={}, port={}", properties.getIp(), properties.getPort());
  }

  private boolean isSslEnabled(Object ssl) {
    if (ValidateUtils.isEmpty(ssl)) {
      return false;
    }
    Object enabled = invokeNoArg(ssl, "isEnabled");
    if (enabled == null) {
      enabled = invokeNoArg(ssl, "getEnabled");
    }
    return Boolean.TRUE.equals(enabled);
  }

  private Object buildSslConfig(Object ssl) {
    if (ValidateUtils.isEmpty(ssl)) {
      return null;
    }
    String keyStorePath = readString(ssl, "getKeystorePath", "getKeyStorePath");
    String keyStorePass = readString(ssl, "getKeystorePass", "getKeyStorePass");
    String trustStorePath = readString(ssl, "getTruststorePath", "getTrustStorePath");
    String trustStorePass = readString(ssl, "getTruststorePass", "getTrustStorePass");
    if (StrUtil.isAllBlank(keyStorePath, trustStorePath)) {
      return null;
    }
    for (String className : List.of(
        "org.tio.core.ssl.SslConfig",
        "org.dromara.mica.mqtt.core.ssl.SslConfig",
        "org.dromara.mica.mqtt.core.common.SslConfig")) {
      Object sslConfig = buildSslConfig(className, keyStorePath, keyStorePass, trustStorePath, trustStorePass);
      if (sslConfig != null) {
        return sslConfig;
      }
    }
    logger.warn("Mica MQTT SSL 证书配置未找到兼容的 SslConfig 类型，将仅启用 useSsl()");
    return null;
  }

  private Object buildSslConfig(
      String className,
      String keyStorePath,
      String keyStorePass,
      String trustStorePath,
      String trustStorePass) {
    try {
      Class<?> sslConfigClass = Class.forName(className);
      for (Method method : sslConfigClass.getMethods()) {
        if (!"forClient".equals(method.getName())) {
          continue;
        }
        if (method.getParameterCount() == 4) {
          return method.invoke(null, keyStorePath, keyStorePass, trustStorePath, trustStorePass);
        }
        if (method.getParameterCount() == 2 && StrUtil.isNotBlank(trustStorePath)) {
          return method.invoke(null, trustStorePath, trustStorePass);
        }
      }
    } catch (ReflectiveOperationException e) {
      logger.debug("跳过不兼容的 Mica MQTT SslConfig 类型: {}", className);
    }
    return null;
  }

  private String readString(Object target, String... methodNames) {
    for (String methodName : methodNames) {
      Object value = invokeNoArg(target, methodName);
      if (value instanceof String) {
        return (String) value;
      }
    }
    return null;
  }

  private Object invokeNoArg(Object target, String methodName) {
    if (ValidateUtils.isEmpty(target)) {
      return null;
    }
    try {
      Method method = target.getClass().getMethod(methodName);
      return method.invoke(target);
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }

  private void invokeOneArg(Object target, String methodName, Object argument) {
    try {
      for (Method method : target.getClass().getMethods()) {
        if (methodName.equals(method.getName()) && method.getParameterCount() == 1) {
          method.invoke(target, argument);
          return;
        }
      }
    } catch (ReflectiveOperationException e) {
      logger.warn("调用 Mica MQTT SSL 方法失败: {}", methodName, e);
    }
  }

  @SuppressWarnings("unchecked")
  private AbstractConsumer<?, ?> copyConsumer(Object listener) {
    return (AbstractConsumer<?, ?>) BeanUtil.copyProperties(listener, listener.getClass(), (String) null);
  }
}
