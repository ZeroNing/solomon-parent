package com.steven.solomon.config;

import com.steven.solomon.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(XxlJobProperties.class)
public class XxlJobConfig {

    @Bean
    @ConditionalOnMissingBean(XxlJobSpringExecutor.class)
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties profile) throws Exception {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(profile.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(profile.getAppName());
        xxlJobSpringExecutor.setIp(profile.getIp());
        xxlJobSpringExecutor.setPort(profile.getPort());
        xxlJobSpringExecutor.setAccessToken(profile.getAccessToken());
        xxlJobSpringExecutor.setLogPath(profile.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(profile.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }
}
