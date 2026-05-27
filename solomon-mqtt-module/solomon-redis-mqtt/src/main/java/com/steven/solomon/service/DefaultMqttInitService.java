package com.steven.solomon.service;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.profile.RedisMqttProfile;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

public class DefaultMqttInitService implements MqttClientInitService<RedisMqttProfile> {

  private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

  private final MqttUtils utils;

  public DefaultMqttInitService(MqttUtils utils) {
    this.utils = utils;
  }

  @Override
  public void initMqttClient(String tenantCode, RedisMqttProfile profile) throws Exception {
    initMqttClient(tenantCode, profile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
  }

  @Override
  public void initMqttClient(String tenantCode, RedisMqttProfile profile, List<Object> listenerList) {
    RedisMessageListenerContainer container = utils.createContainer(tenantCode, profile);
    utils.subscribe(container, listenerList, tenantCode);
    logger.info("租户:{} Redis MQTT 客户端初始化成功", tenantCode);
  }
}
