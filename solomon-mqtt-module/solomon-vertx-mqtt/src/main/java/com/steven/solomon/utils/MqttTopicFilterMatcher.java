package com.steven.solomon.utils;

import com.steven.solomon.verification.ValidateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MQTT 主题通配符匹配器。
 *
 * <p>实现 MQTT 规范的主题过滤器匹配逻辑，支持单级通配符 {@code +} 和多级通配符 {@code #}。</p>
 * <p>主要用于 Vert.x MQTT 实现，因为 Vert.x 不支持 Paho 风格的自动主题路由，需要手动匹配。</p>
 */
public class MqttTopicFilterMatcher {

    private MqttTopicFilterMatcher() {}

    // ====================== 核心API ======================
    /**
     * 查找匹配主题的第一个通配符过滤器。
     *
     * @param topic 实际主题 (e.g. "sensor/room1/temperature")
     * @param filters 通配符主题过滤器集合 (e.g. ["sensor/+/temperature", "alarm/#"])
     * @return 匹配成功的过滤器，若无匹配返回 null
     */
    public static String findFirstMatchingFilter(String topic, List<String> filters) {
        for (String filter : filters) {
            if (matches(filter, topic)) {
                return filter;
            }
        }
        return null;
    }

    /**
     * 查找所有匹配主题的通配符过滤器。
     *
     * @param topic 实际主题
     * @param filters 通配符主题过滤器集合
     * @return 所有匹配成功的过滤器列表（保持原顺序）
     */
    public static List<String> findAllMatchingFilters(String topic, List<String> filters) {
        if (ValidateUtils.isEmpty(topic) || ValidateUtils.isEmpty(filters)) {
            return Collections.emptyList();
        }

        List<String> matches = new ArrayList<>();
        for (String filter : filters) {
            if (matches(filter, topic)) {
                matches.add(filter);
            }
        }
        return matches;
    }

    // ====================== 底层匹配逻辑 ======================
    /**
     * 判断主题是否匹配单个通配符过滤器。
     *
     * @param filter 通配符主题过滤器 (e.g. "sensor/+/temperature")
     * @param topic 实际主题 (e.g. "sensor/room1/temperature")
     * @return true 如果匹配成功
     */
    public static boolean matches(String filter, String topic) {
        // 空检查（MQTT规范禁止空主题）
        if (ValidateUtils.isEmpty(filter) || ValidateUtils.isEmpty(topic)) {
            return false;
        }

        // 分割层级（保留末尾空字符串以检测非法结尾'/'）
        String[] filterLevels = filter.split("/", -1);
        String[] topicLevels = topic.split("/", -1);

        // 检查过滤器合法性（纯文本主题也需验证）
        if (!isValidFilter(filterLevels)) {
            return false;
        }

        // 逐层匹配
        int i = 0;
        while (i < filterLevels.length && i < topicLevels.length) {
            String f = filterLevels[i];
            String t = topicLevels[i];

            // 处理非法空层级（规范禁止）
            if (t.isEmpty()) {
                return false;
            }

            if (f.equals("+")) {
                // '+' 匹配任意单层非空主题
                i++;
            } else if (f.equals("#")) {
                // '#' 必须位于末尾（已在isValidFilter中验证）
                return true;
            } else {
                // 严格字符串匹配（兼容纯文本主题）
                if (!f.equals(t)) {
                    return false;
                }
                i++;
            }
        }

        // 检查层级数是否一致
        return i == filterLevels.length && i == topicLevels.length;
    }

    /**
     * 验证主题过滤器是否符合 MQTT 规范。
     *
     * <p>检查 {@code #} 只能出现在末尾，且过滤器中不含非法空层级。</p>
     *
     * @param levels 按层级分割的过滤器数组
     * @return 过滤器是否合法
     */
    private static boolean isValidFilter(String[] levels) {
        boolean hasMultiLevelWildcard = false;

        for (int i = 0; i < levels.length; i++) {
            String level = levels[i];

            // 检查非法空层级（末尾空字符串允许表示以'/'结尾）
            if (level.isEmpty() && i < levels.length - 1) {
                return false;
            }

            // 检查多级通配符 #
            if (level.equals("#")) {
                if (hasMultiLevelWildcard || i != levels.length - 1) {
                    return false; // '#' 必须是最后一个层级
                }
                hasMultiLevelWildcard = true;
            }
            // 单级通配符+无需额外检查
        }
        return true;
    }

    // ====================== 使用示例 ======================
    public static void main(String[] args) {
        // 测试数据
        String topic = "sensor/room1/temperature";
        List<String> filters = List.of(
                "sensor/+/temperature",  // 通配符匹配
                "sensor/room1/temperature", // 纯文本匹配
                "alarm/#",
                "sensor/room1/+"
        );

        // 场景1：获取第一个匹配的过滤器
        String firstMatch = findFirstMatchingFilter(topic, filters);
        // 场景2：获取所有匹配的过滤器
        List<String> allMatches = findAllMatchingFilters(topic, filters);
        // 验证纯文本主题匹配
        assert matches("sensor/room1/temperature", "sensor/room1/temperature"); // true
        assert !matches("sensor/room1/temp", "sensor/room1/temperature");     // false
    }
}
