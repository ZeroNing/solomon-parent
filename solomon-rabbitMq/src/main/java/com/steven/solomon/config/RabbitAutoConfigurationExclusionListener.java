package com.steven.solomon.config;

import cn.hutool.core.util.BooleanUtil;
import com.steven.solomon.config.i18n.AutoConfigurationExclusionListener;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
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
