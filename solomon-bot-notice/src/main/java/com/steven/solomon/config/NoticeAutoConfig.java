package com.steven.solomon.config;

import com.steven.solomon.service.DingTalkService;
import com.steven.solomon.service.FeishuService;
import com.steven.solomon.service.NoticeService;
import com.steven.solomon.service.WechatWorkService;
import com.steven.solomon.utils.NoticeUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 机器人通知自动配置。
 *
 * <p>根据 {@link NoticeProperties} 配置，自动注册钉钉、企业微信、飞书机器人的通知服务，
 * 以及聚合工具 {@link NoticeUtils}。业务项目可以通过声明同名 Bean 来覆盖默认实现。</p>
 */
@Configuration
@EnableConfigurationProperties(NoticeProperties.class)
@ConditionalOnProperty(name = "solomon.notice.enabled", havingValue = "true", matchIfMissing = true)
public class NoticeAutoConfig {

    /**
     * 注册钉钉机器人通知服务。
     *
     * @param properties 通知配置属性
     * @return 钉钉通知服务实例
     */
    @Bean
    @ConditionalOnMissingBean(DingTalkService.class)
    public DingTalkService dingTalkService(NoticeProperties properties) {
        return new DingTalkService(properties);
    }

    /**
     * 注册企业微信机器人通知服务。
     *
     * @param properties 通知配置属性
     * @return 企业微信通知服务实例
     */
    @Bean
    @ConditionalOnMissingBean(WechatWorkService.class)
    public WechatWorkService wechatWorkService(NoticeProperties properties) {
        return new WechatWorkService(properties);
    }

    /**
     * 注册飞书机器人通知服务。
     *
     * @param properties 通知配置属性
     * @return 飞书通知服务实例
     */
    @Bean
    @ConditionalOnMissingBean(FeishuService.class)
    public FeishuService feishuService(NoticeProperties properties) {
        return new FeishuService(properties);
    }

    /**
     * 注册通知聚合工具。
     *
     * @param serviceList 所有已注册的通知服务列表
     * @param properties   通知配置属性
     * @return 通知聚合工具实例
     */
    @Bean
    @ConditionalOnMissingBean(NoticeUtils.class)
    public NoticeUtils noticeUtils(List<NoticeService> serviceList, NoticeProperties properties) {
        return new NoticeUtils(serviceList, properties);
    }
}
