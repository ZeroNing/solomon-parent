package com.steven.solomon.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.cache.redis.factory.RedisConnectionFactoryBuilder;
import com.steven.solomon.cache.redis.context.RedisCacheTenantContext;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.mqtt.AbstractMqttClientRegistry;
import com.steven.solomon.mqtt.MqttOperations;
import com.steven.solomon.mqtt.code.MqttErrorCodes;
import com.steven.solomon.mqtt.model.MqttMessageModel;
import com.steven.solomon.mqtt.model.MqttSubscriptionDescriptor;
import com.steven.solomon.mqtt.support.MqttListenerRegistry;
import com.steven.solomon.profile.RedisMqttProfile;
import com.steven.solomon.redis.mqtt.consumer.AbstractRedisMqttConsumer;
import com.steven.solomon.service.SendService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

@Configuration
public class MqttUtils
    extends AbstractMqttClientRegistry<RedisMessageListenerContainer, RedisMqttProfile>
    implements SendService<MqttMessageModel<?>>, MqttOperations {

  private static final String CACHE_TENANT_PREFIX = "mqtt:";

  private final Logger logger = LoggerUtils.logger(MqttUtils.class);

  private final Map<String, StringRedisTemplate> redisTemplateMap = new ConcurrentHashMap<>();

  private final Map<String, RedisConnectionFactory> connectionFactoryMap = new ConcurrentHashMap<>();

  private final Map<String, Map<String, org.springframework.data.redis.connection.MessageListener>> listenerMap =
      new ConcurrentHashMap<>();

  private final RedisConnectionFactoryBuilder connectionFactoryBuilder;

  private final RedisCacheTenantContext redisCacheTenantContext;

  public MqttUtils(
      RedisConnectionFactoryBuilder connectionFactoryBuilder,
      RedisCacheTenantContext redisCacheTenantContext) {
    this.connectionFactoryBuilder = connectionFactoryBuilder;
    this.redisCacheTenantContext = redisCacheTenantContext;
  }

  @Override
  public void send(MqttMessageModel<?> data) throws BaseException {
    RedisMqttProfile profile = getOptions(data.getTenantCode());
    String channel = RedisMqttTopicUtils.channel(data.getTenantCode(), data.getTopic(), profile);
    String payload = JSONUtil.toJsonStr(data);
    getRedisTemplate(data.getTenantCode()).convertAndSend(channel, payload);
    logger.debug("Redis MQTT 消息发送成功, tenant={}, channel={}", data.getTenantCode(), channel);
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
    RedisMqttProfile profile = getOptions(tenantCode);
    RedisMessageListenerContainer container = getClient(tenantCode);
    Topic redisTopic = toRedisTopic(tenantCode, topic, profile);
    org.springframework.data.redis.connection.MessageListener listener =
        (org.springframework.data.redis.connection.MessageListener) consumer;
    container.addMessageListener(listener, redisTopic);
    tenantListeners(tenantCode).put(redisTopic.getTopic(), listener);
    logger.info("租户:{} 订阅 Redis MQTT 通道:{}", tenantCode, redisTopic.getTopic());
  }

  public void subscribe(String tenantCode, String topic, int qos, AbstractRedisMqttConsumer<?, ?> consumer)
      throws Exception {
    subscribe(tenantCode, topic, qos, (Object) consumer);
  }

  public void subscribe(RedisMessageListenerContainer container, String tenantCode) {
    subscribe(container, SpringUtil.getBeanListWithAnnotation(
        com.steven.solomon.mqtt.annotation.MessageListener.class), tenantCode);
  }

  public void subscribe(
      RedisMessageListenerContainer container, List<Object> listenerList, String tenantCode) {
    RedisMqttProfile profile = getOptions(tenantCode);
    for (MqttSubscriptionDescriptor descriptor : MqttListenerRegistry.resolve(tenantCode, listenerList)) {
      Topic redisTopic = toRedisTopic(tenantCode, descriptor.getTopic(), profile);
      org.springframework.data.redis.connection.MessageListener consumer = copyConsumer(descriptor.getListener());
      container.addMessageListener(consumer, redisTopic);
      tenantListeners(tenantCode).put(redisTopic.getTopic(), consumer);
      logger.info("租户:{} 订阅 Redis MQTT 通道:{}", tenantCode, redisTopic.getTopic());
    }
  }

  @Override
  public void unsubscribe(String tenantCode, String[] topics) throws Exception {
    if (ValidateUtils.isEmpty(topics)) {
      return;
    }
    RedisMessageListenerContainer container = getClient(tenantCode);
    RedisMqttProfile profile = getOptions(tenantCode);
    Map<String, org.springframework.data.redis.connection.MessageListener> tenantListenerMap =
        tenantListeners(tenantCode);
    for (String topic : topics) {
      Topic redisTopic = toRedisTopic(tenantCode, topic, profile);
      org.springframework.data.redis.connection.MessageListener listener =
          tenantListenerMap.remove(redisTopic.getTopic());
      if (ValidateUtils.isNotEmpty(listener)) {
        container.removeMessageListener(listener, redisTopic);
      }
    }
  }

  @Override
  public void disconnect(String tenantCode) throws Exception {
    RedisMessageListenerContainer container = getClient(tenantCode);
    container.stop();
    container.destroy();
    listenerMap.remove(tenantCode);
    redisTemplateMap.remove(tenantCode);
    RedisConnectionFactory factory = connectionFactoryMap.remove(tenantCode);
    if (ValidateUtils.isNotEmpty(factory)) {
      destroyConnectionFactory(factory);
    }
    redisCacheTenantContext.unregister(cacheTenantCode(tenantCode));
    removeClient(tenantCode);
  }

  @Override
  public void reconnect(String tenantCode) throws Exception {
    RedisMessageListenerContainer container = getClient(tenantCode);
    if (!container.isRunning()) {
      container.start();
    }
  }

  public RedisMessageListenerContainer createContainer(String tenantCode, RedisMqttProfile profile) {
    RedisConnectionFactory factory = createConnectionFactory(tenantCode, profile);
    StringRedisTemplate redisTemplate = new StringRedisTemplate(factory);
    redisTemplate.afterPropertiesSet();

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(factory);
    container.afterPropertiesSet();
    container.start();

    putOptionsMap(tenantCode, profile);
    putClient(tenantCode, container);
    connectionFactoryMap.put(tenantCode, factory);
    redisTemplateMap.put(tenantCode, redisTemplate);
    logger.info("租户:{} Redis MQTT 连接已创建, host={}, port={}, database={}, ssl={}",
        tenantCode, profile.getHost(), profile.getPort(), profile.getDatabase(), profile.isSsl());
    return container;
  }

  private RedisConnectionFactory createConnectionFactory(String tenantCode, RedisMqttProfile profile) {
    RedisProperties redisProperties = new RedisProperties();
    redisProperties.setHost(profile.getHost());
    redisProperties.setPort(profile.getPort());
    redisProperties.setDatabase(profile.getDatabase());
    if (ValidateUtils.isNotEmpty(profile.getUsername())) {
      redisProperties.setUsername(profile.getUsername());
    }
    if (ValidateUtils.isNotEmpty(profile.getPassword())) {
      redisProperties.setPassword(profile.getPassword());
    }
    if (profile.getTimeout() > 0) {
      redisProperties.setTimeout(Duration.ofMillis(profile.getTimeout()));
    }
    redisProperties.getSsl().setEnabled(profile.isSsl());
    RedisConnectionFactory factory = connectionFactoryBuilder.build(redisProperties);
    redisCacheTenantContext.register(cacheTenantCode(tenantCode), factory);
    return factory;
  }

  private String cacheTenantCode(String tenantCode) {
    return CACHE_TENANT_PREFIX + tenantCode;
  }

  private void destroyConnectionFactory(RedisConnectionFactory factory) {
    if (factory instanceof DisposableBean disposableBean) {
      try {
        disposableBean.destroy();
      } catch (Exception ex) {
        logger.warn("Redis MQTT 连接工厂关闭失败", ex);
      }
    }
  }

  private RedisMessageListenerContainer getClient(String tenantCode) throws BaseException {
    return getRequiredClient(tenantCode, MqttErrorCodes.CLIENT_IS_NULL);
  }

  private StringRedisTemplate getRedisTemplate(String tenantCode) throws BaseException {
    StringRedisTemplate redisTemplate = redisTemplateMap.get(tenantCode);
    if (ValidateUtils.isEmpty(redisTemplate)) {
      throw new BaseException(MqttErrorCodes.CLIENT_IS_NULL, tenantCode);
    }
    return redisTemplate;
  }

  private Topic toRedisTopic(String tenantCode, String topic, RedisMqttProfile profile) {
    String channel = RedisMqttTopicUtils.channel(tenantCode, topic, profile);
    if (RedisMqttTopicUtils.needPattern(topic, profile)) {
      return new PatternTopic(RedisMqttTopicUtils.pattern(channel));
    }
    return new ChannelTopic(channel);
  }

  private Map<String, org.springframework.data.redis.connection.MessageListener> tenantListeners(String tenantCode) {
    return listenerMap.computeIfAbsent(tenantCode, key -> new ConcurrentHashMap<>());
  }

  @SuppressWarnings("unchecked")
  private org.springframework.data.redis.connection.MessageListener copyConsumer(Object listener) {
    return (org.springframework.data.redis.connection.MessageListener) BeanUtil.copyProperties(
        listener, listener.getClass(), (String) null);
  }
}
