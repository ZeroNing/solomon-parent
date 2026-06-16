package com.steven.solomon.cloud.sentinel;

import com.steven.solomon.cloud.sentinel.handler.SentinelBlockExceptionHandler;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Sentinel 限流熔断自动配置。
 *
 * <p>当 classpath 存在 Sentinel 且 {@code sentinel.enabled=true}（默认开启）时生效。</p>
 *
 * <p>提供两种限流能力：</p>
 * <ul>
 *   <li>服务端：业务方法用 {@code @SentinelResource} 标记，Sentinel 按资源名限流。</li>
 *   <li>网关端：Spring Cloud Gateway 集成 Sentinel 全局过滤器，按路由维度限流。
 *       限流规则从 Nacos 动态加载，支持运行期热更新。</li>
 * </ul>
 *
 * <p>限流/熔断触发时，统一由 {@link SentinelBlockExceptionHandler} 返回中文错误响应，
 * 与项目国际化文案保持一致。</p>
 *
 * @author steven
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.alibaba.csp.sentinel.SphU")
@ConditionalOnProperty(name = "sentinel.enabled", havingValue = "true", matchIfMissing = true)
public class SentinelAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(SentinelAutoConfiguration.class);

    public SentinelAutoConfiguration() {
        logger.info("Sentinel 限流熔断模块已启用，业务侧使用 @SentinelResource 保护资源");
    }

    /**
     * 注册统一的限流熔断异常处理器。
     *
     * <p>被 Sentinel 拦截的请求（限流、降级、熔断）触发 BlockException 时，
     * 由本处理器转换为统一的中文错误响应，避免直接抛出英文异常或空白页。</p>
     */
    @Bean
    @ConditionalOnMissingBean(SentinelBlockExceptionHandler.class)
    public SentinelBlockExceptionHandler sentinelBlockExceptionHandler() {
        logger.info("注册 Sentinel 限流熔断异常处理器");
        return new SentinelBlockExceptionHandler();
    }
}
