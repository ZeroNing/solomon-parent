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
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttUtils extends AbstractMqttClientRegistry<MqttClient, MqttConnectOptions>
    implements SendService<MqttMessageModel<?>>, MqttOperations {

  private final Logger logger = LoggerUtils.logger(MqttUtils.class);

  /**
   * 发送 MQTT 消息，发送失败时向业务侧抛出异常，避免调用方误判成功。
   */
  @Override
  public void send(MqttMessageModel<?> data) throws Exception {
    String json = JSONUtil.toJsonStr(data);
    try {
      getClient(data.getTenantCode())
          .getTopic(data.getTopic())
          .publish(json.getBytes(StandardCharsets.UTF_8), data.getQos(), data.getRetained());
    } catch (MqttException e) {
      logger.error("MQTT 消息发送失败, tenant={}, topic={}, payload={}",
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
  public void subscribe(String tenantCode, String topic, int qos, Object consumer)
      throws MqttException, BaseException {
    if (ValidateUtils.isEmpty(topic)) {
      return;
    }
    getClient(tenantCode).subscribe(topic, qos, (IMqttMessageListener) consumer);
  }

  public void subscribe(String tenantCode, String topic, int qos, IMqttMessageListener consumer)
      throws MqttException, BaseException {
    subscribe(tenantCode, topic, qos, (Object) consumer);
  }

  public void subscribe(MqttClient client, String tenantCode) throws MqttException {
    subscribe(client, SpringUtil.getBeanListWithAnnotation(
        com.steven.solomon.mqtt.annotation.MessageListener.class), tenantCode);
  }

  public void subscribe(MqttClient client, List<Object> listenerList, String tenantCode)
      throws MqttException {
    for (MqttSubscriptionDescriptor descriptor : MqttListenerRegistry.resolve(tenantCode, listenerList)) {
      AbstractConsumer<?, ?> consumer = copyConsumer(descriptor.getListener());
      client.subscribe(descriptor.getTopic(), descriptor.getQos(), consumer);
      logger.info("租户:{} 订阅 MQTT3 主题:{}", tenantCode, descriptor.getTopic());
    }
  }

  @Override
  public void unsubscribe(String tenantCode, String[] topics) throws MqttException, BaseException {
    if (ValidateUtils.isEmpty(topics)) {
      return;
    }
    getClient(tenantCode).unsubscribe(topics);
  }

  @Override
  public void disconnect(String tenantCode) throws MqttException, BaseException {
    MqttClient client = getClient(tenantCode);
    if (client.isConnected()) {
      client.disconnect();
    }
    removeClient(tenantCode);
  }

  @Override
  public void reconnect(String tenantCode) throws MqttException, BaseException {
    MqttClient client = getClient(tenantCode);
    if (!client.isConnected()) {
      client.connect(getOptions(tenantCode));
      subscribe(client, tenantCode);
    }
  }

  public void reconnect(String tenantCode, MqttProfile mqttProfile) throws MqttException, BaseException {
    MqttClient client = getClient(tenantCode);
    if (!client.isConnected()) {
      client.connect(initMqttConnectOptions(mqttProfile));
      subscribe(client, tenantCode);
    }
  }

  private MqttClient getClient(String tenantCode) throws BaseException {
    return getRequiredClient(tenantCode, MqttErrorCodes.CLIENT_IS_NULL);
  }

  public MqttConnectOptions initMqttConnectOptions(MqttProfile mqttProfile) {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setUserName(mqttProfile.getUserName());
    options.setPassword(mqttProfile.getPassword().toCharArray());
    options.setServerURIs(mqttProfile.getUrl().split(","));
    options.setMaxInflight(mqttProfile.getMaxInflight());
    options.setConnectionTimeout(mqttProfile.getConnectionTimeout());
    options.setAutomaticReconnect(mqttProfile.getAutomaticReconnect());
    options.setCleanSession(mqttProfile.getCleanSession());
    options.setKeepAliveInterval(mqttProfile.getKeepAliveInterval());
    options.setMaxReconnectDelay(mqttProfile.getMaxReconnectDelay());
    options.setExecutorServiceTimeout(mqttProfile.getExecutorServiceTimeout());

    MqttWill will = mqttProfile.getWill();
    if (ValidateUtils.isNotEmpty(will)) {
      options.setWill(
          will.getTopic(),
          will.getMessage().getBytes(StandardCharsets.UTF_8),
          will.getQos(),
          will.getRetained());
    }
    if (!mqttProfile.getVerifyCertificate()) {
      options.setSocketFactory(MqttSslFactory.trustAllSocketFactory());
    }
    return options;
  }

  @SuppressWarnings("unchecked")
  private AbstractConsumer<?, ?> copyConsumer(Object listener) {
    return (AbstractConsumer<?, ?>) BeanUtil.copyProperties(listener, listener.getClass(), (String) null);
  }
}
