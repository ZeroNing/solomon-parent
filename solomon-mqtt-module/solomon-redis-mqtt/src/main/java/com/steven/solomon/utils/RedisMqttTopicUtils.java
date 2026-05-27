package com.steven.solomon.utils;

import com.steven.solomon.profile.RedisMqttProfile;
import com.steven.solomon.verification.ValidateUtils;

/**
 * Redis MQTT 主题工具。
 */
public final class RedisMqttTopicUtils {

  private RedisMqttTopicUtils() {
  }

  /**
   * 构造 Redis 通道名。
   */
  public static String channel(String tenantCode, String topic, RedisMqttProfile profile) {
    StringBuilder builder = new StringBuilder();
    if (ValidateUtils.isNotEmpty(profile) && ValidateUtils.isNotEmpty(profile.getChannelPrefix())) {
      builder.append(trimSlash(profile.getChannelPrefix())).append(":");
    }
    if (ValidateUtils.isEmpty(profile) || profile.isUseTenantPrefix()) {
      builder.append(tenantCode).append(":");
    }
    return builder.append(topic).toString();
  }

  /**
   * 将 MQTT 通配符转换成 Redis PatternTopic 通配符。
   */
  public static String pattern(String topic) {
    return topic.replace("+", "*").replace("#", "*");
  }

  public static boolean needPattern(String topic, RedisMqttProfile profile) {
    return (ValidateUtils.isNotEmpty(profile) && profile.isPatternTopic())
        || topic.contains("+")
        || topic.contains("#")
        || topic.contains("*");
  }

  private static String trimSlash(String value) {
    while (value.endsWith(":")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }
}
