package com.steven.solomon.mqtt.redis.utils;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.mqtt.redis.profile.RedisMqttProfile;

/**
 * Redis MQTT 主题工具类。
 *
 * <p>负责 MQTT 主题与 Redis 通道之间的转换，包括通道名构造、通配符映射和是否需要 PatternTopic 的判断。</p>
 */
public final class RedisMqttTopicUtils {

  private RedisMqttTopicUtils() {
  }

  /**
   * 根据 MQTT 主题和租户配置构造 Redis 通道名。
   *
   * <p>通道名格式为：{@code channelPrefix:tenantCode:topic} 或 {@code tenantCode:topic}（无前缀时）。</p>
   *
   * @param tenantCode 租户编码
   * @param topic MQTT 订阅主题
   * @param profile Redis MQTT 配置
   * @return Redis 通道名
   */
  public static String channel(String tenantCode, String topic, RedisMqttProfile profile) {
    StringBuilder builder = new StringBuilder();
    if (ObjectUtil.isNotEmpty(profile) && ObjectUtil.isNotEmpty(profile.getChannelPrefix())) {
      builder.append(trimSlash(profile.getChannelPrefix())).append(":");
    }
    if (ObjectUtil.isEmpty(profile) || profile.isUseTenantPrefix()) {
      builder.append(tenantCode).append(":");
    }
    return builder.append(topic).toString();
  }

  /**
   * 将 MQTT 通配符转换成 Redis PatternTopic 通配符。
   *
   * <p>MQTT 的 {@code +} 和 {@code #} 统一转换为 Redis glob 模式的 {@code *}。</p>
   *
   * @param topic 包含 MQTT 通配符的主题
   * @return 转换后的 Redis 模式通配符主题
   */
  public static String pattern(String topic) {
    return topic.replace("+", "*").replace("#", "*");
  }

  /**
   * 判断是否需要使用 Redis PatternTopic 订阅。
   *
   * <p>当配置强制开启 patternTopic，或主题中包含 MQTT/Redis 通配符时返回 true。</p>
   *
   * @param topic 订阅主题
   * @param profile Redis MQTT 配置
   * @return 是否需要使用 PatternTopic
   */
  public static boolean needPattern(String topic, RedisMqttProfile profile) {
    return (ObjectUtil.isNotEmpty(profile) && profile.isPatternTopic())
        || topic.contains("+")
        || topic.contains("#")
        || topic.contains("*");
  }

  /**
   * 去除字符串末尾的冒号分隔符。
   */
  private static String trimSlash(String value) {
    while (value.endsWith(":")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }
}
