package com.steven.solomon.config;

import com.steven.solomon.config.component.AutoConfigurationExclusionListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB自动配置排除监听器。
 *
 * <p>当MongoDB功能禁用时（{@code spring.data.mongodb.enabled=false}），
 * 自动排除Spring Boot内置的 {@code MongoAutoConfiguration}，
 * 避免在未配置MongoDB时启动报错。</p>
 */
@Component
public class MongoDbConfigurationExclusionListener extends AutoConfigurationExclusionListener {

    /**
     * 获取MongoDB启用开关的配置key。
     * @return 配置key
     */
    @Override
    public String getEnabledKey() {
        return "spring.data.mongodb.enabled";
    }

    /**
     * 获取组件名称。
     * @return 组件名称
     */
    @Override
    public String getComponentName() {
        return "MongoDb";
    }

    /**
     * 获取需要排除的自动配置类。
     * @return 排除配置映射
     */
    @Override
    public Map<String, Object> getExclude() {
        Map<String, Object> map = new HashMap<>();
        map.put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration");
        return map;
    }
}
