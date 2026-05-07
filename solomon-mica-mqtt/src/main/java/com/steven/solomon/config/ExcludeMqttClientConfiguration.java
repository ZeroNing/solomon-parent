package com.steven.solomon.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.annotation.Configuration;

public class ExcludeMqttClientConfiguration implements AutoConfigurationImportFilter {
    // 这里填写你要禁用的 Mica MQTT 自动配置类的全限定名
    private static final String MICA_MQTT_CLIENT_CONFIG =
            "org.dromara.mica.mqtt.spring.client.config.MqttClientConfiguration";

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
