package com.steven.solomon.config;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(XxlJobProperties.class)
public class XxlJobConfig {

    @Bean
    @ConditionalOnMissingBean(XxlJobSpringExecutor.class)
    @Conditional(XxlJobCondition.class)
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties profile) throws Exception {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(profile.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(profile.getAppName());
        if (ObjectUtil.isNotEmpty(profile.getAddress())) {
            xxlJobSpringExecutor.setAddress(profile.getAddress());
        }
        xxlJobSpringExecutor.setIp(profile.getIp());
        xxlJobSpringExecutor.setPort(profile.getPort());
        xxlJobSpringExecutor.setAccessToken(profile.getAccessToken());
        if (ObjectUtil.isNotEmpty(profile.getLogPath())) {
            xxlJobSpringExecutor.setLogPath(profile.getLogPath());
        }
        if (ObjectUtil.isNotEmpty(profile.getTimeout())) {
            xxlJobSpringExecutor.setTimeout(profile.getTimeout());
        }
        xxlJobSpringExecutor.setLogRetentionDays(profile.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }
}
