package com.steven.solomon.config.component;

import com.steven.solomon.utils.logger.LoggerUtils;
import cn.hutool.core.util.ObjectUtil;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自动配置排除监听器。
 *
 * <p>子类提供开关配置和需要排除的自动配置项；当开关关闭时，统一写入
 * {@code spring.autoconfigure.exclude} 相关配置。</p>
 */
public abstract class AutoConfigurationExclusionListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    /** 表示“启用”的布尔值集合，包含多种语言的表达方式。 */
    private static final Set<String> TRUE_SET = new HashSet<>(List.of("true", "yes", "y", "t", "ok", "1", "on", "是", "对", "真", "對", "√"));

    public final Logger logger = LoggerUtils.logger(getClass());

    /**
     * 处理应用环境准备事件，检查组件开关并动态排除自动配置。
     *
     * <p>当开关关闭时，将排除配置写入环境属性源，
     * Spring Boot 自动配置机制会读取并跳过这些类。</p>
     *
     * @param event 应用环境准备事件
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String enabledKey = getEnabledKey();
        String enabledValue = event.getEnvironment().getProperty(enabledKey);
        if (ObjectUtil.isEmpty(enabledValue)) {
            enabledValue = "true";
        }
        enabledValue = enabledValue.trim();
        boolean enabled = TRUE_SET.contains(enabledValue);
        logger.info("{}:配置为:{},{}",getComponentName(),enabledValue,enabled ? "不禁用该组件" : "禁用该组件");
        if (enabled) {
           return;
        }
        ConfigurableEnvironment environment = event.getEnvironment();
        Map<String, Object> map = getExclude();
        MapPropertySource propertySource = new MapPropertySource("customProperties", map);
        environment.getPropertySources().addLast(propertySource);
    }

    /**
     * 返回组件启用开关的配置键名。
     *
     * @return 配置键名，例如 "solomon.redis.enabled"
     */
    public abstract String getEnabledKey();

    /**
     * 返回组件名称，用于日志输出。
     *
     * @return 组件名称
     */
    public abstract String getComponentName();

    /**
     * 返回需要排除的自动配置类映射。
     *
     * <p>映射的键为 {@code spring.autoconfigure.exclude}，值为排除类列表。</p>
     *
     * @return 排除配置映射
     */
    public abstract Map<String, Object> getExclude();
}
