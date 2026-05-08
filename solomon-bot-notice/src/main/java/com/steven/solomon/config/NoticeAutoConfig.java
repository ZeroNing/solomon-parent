package com.steven.solomon.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 通知服务自动配置
 */
@Configuration
@EnableConfigurationProperties(NoticeProperties.class)
@ComponentScan(basePackages = "com.steven.solomon")
@ConditionalOnProperty(name = "solomon.notice.enabled", havingValue = "true", matchIfMissing = true)
public class NoticeAutoConfig {

}
