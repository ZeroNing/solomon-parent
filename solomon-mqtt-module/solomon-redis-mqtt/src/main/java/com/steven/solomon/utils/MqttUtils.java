package com.steven.solomon.utils;

import cn.hutool.core.util.ObjectUtil;

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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

/**
 * Redis MQTT 风格工具类。
 *
 * <p>基于 Redis Pub/Sub 实现 MQTT 风格的消息发布/订阅，提供消息发送、主题订阅/取消订阅、连接管理和 Redis 连接工厂创建等功能。</p>
 * <p>同时实现 {@link SendService} 和 {@link MqttOperations} 接口，统一消息发送入口。</p>
 */
@Configuration
public class MqttUtils
    extends AbstractMqttClientRegistry<RedisMessageListenerContainer, RedisMqttProfile>
    implements SendService<MqttMessageModel<?>>, MqttOperations {

  /** Redis 租户缓存前缀 */
  private static final String CACHE_TENANT_PREFIX = "mqtt:";

  /** 日志记录器 */
  private final Logger logger = LoggerUtils.logger(MqttUtils.class);

  /** 租户与 Redis 发送模板的映射 */
  private final Map<String, StringRedisTemplate> redisTemplateMap = new ConcurrentHashMap<>();

  /** 租户与 Redis 连接工厂的映射 */
  private final Map<String, RedisConnectionFactory> connectionFactoryMap = new ConcurrentHashMap<>();

  /** 租户与 Redis 消息监听器的映射 */
  private final Map<String, Map<String, org.springframework.data.redis.connection.MessageListener>> listenerMap =
      new ConcurrentHashMap<>();

  /** Redis 连接工厂构建器 */
  private final RedisConnectionFactoryBuilder connectionFactoryBuilder;

  /** Redis 多租户缓存上下文 */
  private final RedisCacheTenantContext redisCacheTenantContext;

  /**
   * 构造方法。
   *
   * @param connectionFactoryBuilder Redis 连接工厂构建器
   * @param redisCacheTenantContext Redis 多租户缓存上下文
   */
  public MqttUtils(
      RedisConnectionFactoryBuilder connectionFactoryBuilder,
      RedisCacheTenantContext redisCacheTenantContext) {
    this.connectionFactoryBuilder = connectionFactoryBuilder;
    this.redisCacheTenantContext = redisCacheTenantContext;
  }

  /**
   * 通过 Redis Pub/Sub 发送 MQTT 风格消息。
   *
   * @param data 消息模型，包含租户编码、主题和消息体
   */
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

  /**
   * 发送过期消息（Redis Pub/Sub 不支持过期，直接发送）。
   */
  @Override
  public void sendExpiration(MqttMessageModel<?> data, long expiration) throws Exception {
    send(data);
  }

  /**
   * 订阅指定租户的 Redis MQTT 通道。
   *
   * @param tenantCode 租户编码
   * @param topic 订阅主题
   * @param qos 消息质量等级（Redis 实现忽略此参数）
   * @param consumer 消费者实例
   */
  @Override
  public void subscribe(String tenantCode, String topic, int qos, Object consumer) throws Exception {
    if (ObjectUtil.isEmpty(topic)) {
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

  /**
   * 订阅指定租户的 Redis MQTT 通道（AbstractRedisMqttConsumer 重载）。
   */
  public void subscribe(String tenantCode, String topic, int qos, AbstractRedisMqttConsumer<?, ?> consumer)
      throws Exception {
    subscribe(tenantCode, topic, qos, (Object) consumer);
  }

  /**
   * 使用自动扫描的监听器订阅指定租户的所有主题。
   */
  public void subscribe(RedisMessageListenerContainer container, String tenantCode) {
    subscribe(container, SpringUtil.getBeanListWithAnnotation(
        com.steven.solomon.mqtt.annotation.MessageListener.class), tenantCode);
  }

  /**
   * 根据监听器列表解析订阅描述，逐一订阅 Redis 通道。
   */
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

  /**
   * 取消订阅指定租户的 Redis MQTT 通道。
   */
  @Override
  public void unsubscribe(String tenantCode, String[] topics) throws Exception {
    if (ObjectUtil.isEmpty(topics)) {
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
      if (ObjectUtil.isNotEmpty(listener)) {
        container.removeMessageListener(listener, redisTopic);
      }
    }
  }

  /**
   * 断开指定租户的 Redis MQTT 连接，销毁连接工厂并清理所有资源。
   */
  @Override
  public void disconnect(String tenantCode) throws Exception {
    RedisMessageListenerContainer container = getClient(tenantCode);
    container.stop();
    container.destroy();
    listenerMap.remove(tenantCode);
    redisTemplateMap.remove(tenantCode);
    RedisConnectionFactory factory = connectionFactoryMap.remove(tenantCode);
    if (ObjectUtil.isNotEmpty(factory)) {
      destroyConnectionFactory(factory);
    }
    redisCacheTenantContext.unregister(cacheTenantCode(tenantCode));
    removeClient(tenantCode);
  }

  /**
   * 重新启动指定租户的 Redis MQTT 监听容器。
   */
  @Override
  public void reconnect(String tenantCode) throws Exception {
    RedisMessageListenerContainer container = getClient(tenantCode);
    if (!container.isRunning()) {
      container.start();
      // 重连后恢复所有订阅
      subscribe(container, tenantCode);
    }
  }

  /**
   * 使用新的配置重建指定租户的 Redis MQTT 连接并恢复订阅。
   *
   * @param tenantCode 租户编码
   * @param profile 新的 Redis MQTT 配置
   */
  public void reconnect(String tenantCode, RedisMqttProfile profile) throws Exception {
    // 先断开旧连接
    disconnect(tenantCode);
    // 用新配置重建连接
    RedisMessageListenerContainer container = createContainer(tenantCode, profile);
    subscribe(container, tenantCode);
  }

  /**
   * 根据配置创建 Redis 连接工厂、发送模板和监听容器。
   *
   * @param tenantCode 租户编码
   * @param profile Redis MQTT 配置
   * @return 已启动的 Redis 消息监听容器
   */
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

  /**
   * 根据配置创建 Redis 连接工厂，并注册到多租户缓存上下文。
   */
  private RedisConnectionFactory createConnectionFactory(String tenantCode, RedisMqttProfile profile) {
    RedisProperties redisProperties = new RedisProperties();
    redisProperties.setHost(profile.getHost());
    redisProperties.setPort(profile.getPort());
    redisProperties.setDatabase(profile.getDatabase());
    if (ObjectUtil.isNotEmpty(profile.getUsername())) {
      redisProperties.setUsername(profile.getUsername());
    }
    if (ObjectUtil.isNotEmpty(profile.getPassword())) {
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

  /**
   * 构造租户缓存编码。
   */
  private String cacheTenantCode(String tenantCode) {
    return CACHE_TENANT_PREFIX + tenantCode;
  }

  /**
   * 销毁 Redis 连接工厂。
   */
  private void destroyConnectionFactory(RedisConnectionFactory factory) {
    if (factory instanceof DisposableBean disposableBean) {
      try {
        disposableBean.destroy();
      } catch (Exception ex) {
        logger.warn("Redis MQTT 连接工厂关闭失败", ex);
      }
    }
  }

  /**
   * 获取指定租户的 Redis 监听容器，不存在时抛出业务异常。
   */
  private RedisMessageListenerContainer getClient(String tenantCode) throws BaseException {
    return getRequiredClient(tenantCode, MqttErrorCodes.CLIENT_IS_NULL);
  }

  /**
   * 获取指定租户的 Redis 发送模板，不存在时抛出业务异常。
   */
  private StringRedisTemplate getRedisTemplate(String tenantCode) throws BaseException {
    StringRedisTemplate redisTemplate = redisTemplateMap.get(tenantCode);
    if (ObjectUtil.isEmpty(redisTemplate)) {
      throw new BaseException(MqttErrorCodes.CLIENT_IS_NULL, tenantCode);
    }
    return redisTemplate;
  }

  /**
   * 将 MQTT 主题转换为 Redis Topic 对象。
   */
  private Topic toRedisTopic(String tenantCode, String topic, RedisMqttProfile profile) {
    String channel = RedisMqttTopicUtils.channel(tenantCode, topic, profile);
    if (RedisMqttTopicUtils.needPattern(topic, profile)) {
      return new PatternTopic(RedisMqttTopicUtils.pattern(channel));
    }
    return new ChannelTopic(channel);
  }

  /**
   * 获取或创建指定租户的监听器映射。
   */
  private Map<String, org.springframework.data.redis.connection.MessageListener> tenantListeners(String tenantCode) {
    return listenerMap.computeIfAbsent(tenantCode, key -> new ConcurrentHashMap<>());
  }

  /**
   * 复制消费者实例，为每个订阅创建独立的消费者对象。
   */
  @SuppressWarnings("unchecked")
  private org.springframework.data.redis.connection.MessageListener copyConsumer(Object listener) {
    return (org.springframework.data.redis.connection.MessageListener) BeanUtil.copyProperties(
        listener, listener.getClass(), (String) null);
  }
}
