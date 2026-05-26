package com.steven.solomon.config;

import com.steven.solomon.service.DingTalkServiceImpl;
import com.steven.solomon.service.FeishuServiceImpl;
import com.steven.solomon.service.NoticeService;
import com.steven.solomon.service.WechatWorkServiceImpl;
import com.steven.solomon.utils.NoticeUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 机器人通知自动配置。
 */
@Configuration
@EnableConfigurationProperties(NoticeProperties.class)
@ConditionalOnProperty(name = "solomon.notice.enabled", havingValue = "true", matchIfMissing = true)
public class NoticeAutoConfig {

    @Bean
    @ConditionalOnMissingBean(DingTalkServiceImpl.class)
    public DingTalkServiceImpl dingTalkService(NoticeProperties properties) {
        return new DingTalkServiceImpl(properties);
    }

    @Bean
    @ConditionalOnMissingBean(WechatWorkServiceImpl.class)
    public WechatWorkServiceImpl wechatWorkService(NoticeProperties properties) {
        return new WechatWorkServiceImpl(properties);
    }

    @Bean
    @ConditionalOnMissingBean(FeishuServiceImpl.class)
    public FeishuServiceImpl feishuService(NoticeProperties properties) {
        return new FeishuServiceImpl(properties);
    }

    @Bean
    @ConditionalOnMissingBean(NoticeUtils.class)
    public NoticeUtils noticeUtils(List<NoticeService> serviceList, NoticeProperties properties) {
        return new NoticeUtils(serviceList, properties);
    }
}
