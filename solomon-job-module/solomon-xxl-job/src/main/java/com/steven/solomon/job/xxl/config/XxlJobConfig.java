package com.steven.solomon.job.xxl.config;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.job.xxl.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * XXL-JOB Executor 自动配置。
 *
 * <p>创建 {@link XxlJobSpringExecutor} Bean，配置管理端地址、应用名称、
 * 注册 IP/端口、访问令牌、日志路径等参数。</p>
 */
@Configuration
@Import(XxlJobProperties.class)
public class XxlJobConfig {

    /**
     * 创建 XXL-JOB Spring Executor Bean。
     *
     * @param profile XXL-JOB 配置属性
     * @return XXL-JOB Spring Executor 实例
     * @throws Exception 配置错误时抛出
     */
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
