package com.steven.solomon.config;

import com.steven.solomon.config.component.AutoConfigurationExclusionListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitAutoConfigurationExclusionListener extends AutoConfigurationExclusionListener {

    @Override
    public String getEnabledKey() {
        return "spring.rabbitmq.enabled";
    }

    @Override
    public String getComponentName() {
        return "RabbitMq";
    }

    @Override
    public Map<String, Object> getExclude() {
        Map<String, Object> map = new HashMap<>();
        map.put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration");
        return map;
    }
}
