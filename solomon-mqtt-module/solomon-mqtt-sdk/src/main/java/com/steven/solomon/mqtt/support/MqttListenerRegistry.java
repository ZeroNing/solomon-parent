package com.steven.solomon.mqtt.support;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.annotation.AnnotationUtil;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.model.MqttSubscriptionDescriptor;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;

/**
 * MQTT 监听器解析器。
 */
public final class MqttListenerRegistry {

  private static final Logger LOGGER = LoggerUtils.logger(MqttListenerRegistry.class);

  private MqttListenerRegistry() {
  }

  /**
   * 按租户解析可用订阅，统一处理注解开关、租户范围和主题表达式。
   */
  public static List<MqttSubscriptionDescriptor> resolve(String tenantCode, List<Object> listenerList) {
    List<MqttSubscriptionDescriptor> descriptors = new ArrayList<>();
    if (ObjectUtil.isEmpty(listenerList)) {
      return descriptors;
    }
    for (Object listener : listenerList) {
      MessageListener annotation = AnnotationUtil.getAnnotation(listener.getClass(), MessageListener.class);
      if (ObjectUtil.isEmpty(annotation) || !annotation.enabled() || ObjectUtil.isEmpty(annotation.topics())) {
        continue;
      }
      List<String> tenantRange = Lambda.toList(
          Arrays.asList(annotation.tenantRange()),
          ValidateUtils::isNotEmpty,
          key -> key);
      if (ObjectUtil.isNotEmpty(tenantRange) && !tenantRange.contains(tenantCode)) {
        LOGGER.info("{} 租户跳过 MQTT 监听器 {}, 支持范围: {}",
            tenantCode, listener.getClass().getSimpleName(), tenantRange.toArray());
        continue;
      }
      for (String rawTopic : annotation.topics()) {
        String topic = SpringUtil.getElValue(rawTopic);
        if (ObjectUtil.isNotEmpty(topic)) {
          descriptors.add(new MqttSubscriptionDescriptor(
              tenantCode, topic, annotation.qos(), listener, annotation));
        }
      }
    }
    return descriptors;
  }
}
