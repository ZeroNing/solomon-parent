package com.steven.solomon.config;

import cn.hutool.core.util.BooleanUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitAutoConfigurationExclusionListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        boolean enabled = BooleanUtil.toBoolean(ValidateUtils.getOrDefault(event.getEnvironment().getProperty("spring.rabbitmq.enabled"),"true"));
        if(enabled){
            return;
        }
        ConfigurableEnvironment environment = event.getEnvironment();
        Map<String, Object> map = new HashMap<>();
        // 设置spring.autoconfigure.exclude属性以排除RabbitAutoConfiguration
        map.put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration");
        MapPropertySource propertySource = new MapPropertySource("customProperties", map);
        environment.getPropertySources().addLast(propertySource);
    }
}
