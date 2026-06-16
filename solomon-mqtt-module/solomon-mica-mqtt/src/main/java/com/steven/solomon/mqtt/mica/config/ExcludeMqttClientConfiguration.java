package com.steven.solomon.mqtt.mica.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.annotation.Configuration;

/**
 * Mica MQTT 自动配置排除过滤器。
 *
 * <p>禁用 Mica MQTT 客户端的默认自动配置，由项目自身的 {@link MqttConfig} 接管多租户初始化逻辑。</p>
 */
public class ExcludeMqttClientConfiguration implements AutoConfigurationImportFilter {

    /** 要禁用的 Mica MQTT 自动配置类全限定名 */
    private static final String MICA_MQTT_CLIENT_CONFIG =
            "org.dromara.mica.mqtt.spring.client.config.MqttClientConfiguration";

    /**
     * 过滤自动配置类，排除 Mica MQTT 默认配置。
     *
     * @param autoConfigurationClasses 候选自动配置类全限定名数组
     * @param autoConfigurationMetadata 自动配置元数据
     * @return 每个类是否匹配的布尔数组
     */
    @Override
    public boolean[] match(String[] autoConfigurationClasses,
                           AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] result = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            // 如果当前类正好是我们要禁用的，则返回 false（表示不加载）
            result[i] = !MICA_MQTT_CLIENT_CONFIG.equals(autoConfigurationClasses[i]);
        }
        return result;
    }
}
