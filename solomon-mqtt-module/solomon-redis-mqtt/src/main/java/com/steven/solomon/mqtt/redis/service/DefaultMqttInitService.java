package com.steven.solomon.mqtt.redis.service;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.mqtt.redis.profile.RedisMqttProfile;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.mqtt.redis.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis MQTT 默认客户端初始化服务。
 *
 * <p>基于 Redis Pub/Sub 实现 MQTT 风格的消息发布/订阅，负责创建 Redis 监听容器并自动订阅监听器。</p>
 */
public class DefaultMqttInitService implements MqttClientInitService<RedisMqttProfile> {

  /** 日志记录器 */
  private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

  /** Redis MQTT 工具类，负责监听容器创建与消息发送 */
  private final MqttUtils utils;

  /**
   * 构造方法。
   *
   * @param utils Redis MQTT 工具类实例
   */
  public DefaultMqttInitService(MqttUtils utils) {
    this.utils = utils;
  }

  /**
   * 初始化指定租户的 Redis MQTT 客户端（自动扫描监听器）。
   *
   * @param tenantCode 租户编码
   * @param profile Redis MQTT 配置
   */
  @Override
  public void initMqttClient(String tenantCode, RedisMqttProfile profile) throws Exception {
    initMqttClient(tenantCode, profile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
  }

  /**
   * 初始化指定租户的 Redis MQTT 客户端并订阅监听器。
   *
   * @param tenantCode 租户编码
   * @param profile Redis MQTT 配置
   * @param listenerList 监听器实例列表
   */
  @Override
  public void initMqttClient(String tenantCode, RedisMqttProfile profile, List<Object> listenerList) {
    RedisMessageListenerContainer container = utils.createContainer(tenantCode, profile);
    utils.subscribe(container, listenerList, tenantCode);
    logger.info("租户:{} Redis MQTT 客户端初始化成功", tenantCode);
  }
}
