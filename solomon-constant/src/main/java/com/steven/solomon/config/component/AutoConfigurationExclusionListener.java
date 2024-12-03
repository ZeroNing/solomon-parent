package com.steven.solomon.config.component;

import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.*;

public abstract class AutoConfigurationExclusionListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Set<String> TRUE_SET = new HashSet<>(List.of("true", "yes", "y", "t", "ok", "1", "on", "是", "对", "真", "對", "√"));

    public final Logger logger = LoggerUtils.logger(getClass());

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String enabledKey = getEnabledKey();
        String enabledValue = event.getEnvironment().getProperty(enabledKey);
        if(null == enabledValue || enabledValue.isEmpty()){
            enabledValue = "true";
        }
        enabledValue = enabledValue.trim();
        boolean enabled = TRUE_SET.contains(enabledValue);
        logger.info("{}:配置为:{},{}",getComponentName(),enabledValue,enabled ? "不禁用该组件" : "禁用该组件");
        if(enabled){
           return;
        }
        ConfigurableEnvironment environment = event.getEnvironment();
        Map<String, Object> map = getExclude();
        MapPropertySource propertySource = new MapPropertySource("customProperties", map);
        environment.getPropertySources().addLast(propertySource);
    }

    public abstract String getEnabledKey();

    public abstract String getComponentName();

    public abstract Map<String, Object> getExclude();
}
