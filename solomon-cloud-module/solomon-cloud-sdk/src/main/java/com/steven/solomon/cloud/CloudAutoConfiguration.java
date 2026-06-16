package com.steven.solomon.cloud;

import com.steven.solomon.utils.logger.LoggerUtils;
import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 微服务公共 SDK 自动配置。
 *
 * <p>注册租户来源解析策略和 Feign 透传拦截器：</p>
 * <ul>
 *   <li>{@link HeaderTenantSourceResolver}：默认从 Gateway 透传的 {@code X-Tenant-Code} 头解析租户编码。</li>
 *   <li>{@link TenantHeaderInterceptor}：Feign 调用时把当前租户编码写入请求头，透传给下游服务。</li>
 * </ul>
 *
 * <p>业务侧可自定义 {@link TenantSourceResolver} Bean 覆盖默认实现，
 * 例如实现从 JWT Token 实时解析租户编码的策略。</p>
 *
 * @author steven
 */
@AutoConfiguration
public class CloudAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(CloudAutoConfiguration.class);

    /**
     * 注册默认的租户来源解析器（从请求头读取 Gateway 透传的租户编码）。
     *
     * <p>业务侧声明自定义 {@link TenantSourceResolver} Bean 时，本默认实现自动让位。</p>
     */
    @Bean
    @ConditionalOnMissingBean(TenantSourceResolver.class)
    public TenantSourceResolver headerTenantSourceResolver() {
        logger.info("注册默认租户来源解析器: HeaderTenantSourceResolver（从 X-Tenant-Code 请求头读取）");
        return new HeaderTenantSourceResolver();
    }

    /**
     * 注册 Feign 租户透传拦截器（仅当 classpath 存在 Feign 时生效）。
     */
    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    @ConditionalOnMissingBean(TenantHeaderInterceptor.class)
    public RequestInterceptor tenantHeaderInterceptor() {
        logger.info("注册 Feign 租户透传拦截器: TenantHeaderInterceptor");
        return new TenantHeaderInterceptor();
    }
}
