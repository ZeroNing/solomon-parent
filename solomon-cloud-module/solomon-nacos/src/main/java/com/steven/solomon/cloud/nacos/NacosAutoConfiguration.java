package com.steven.solomon.cloud.nacos;

import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.cloud.nacos.service.NacosServiceSubscriber;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Nacos 自动配置。
 *
 * <p>当 classpath 存在 Nacos 客户端且 {@code nacos.enabled=true}（默认开启）时生效。
 * 自动注册 {@link NacosServiceSubscriber}，在服务启动时打印注册中心与服务实例信息，
 * 便于运维快速确认服务注册状态。</p>
 *
 * <p>Nacos 的服务发现、动态配置能力由 spring-cloud-starter-alibaba-nacos-* 自动装配，
 * 业务侧只需在 {@code bootstrap.yml} 配置 Nacos 地址即可。</p>
 *
 * @author steven
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.alibaba.nacos.api.NacosFactory")
@ConditionalOnProperty(name = "nacos.enabled", havingValue = "true", matchIfMissing = true)
public class NacosAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(NacosAutoConfiguration.class);

    /**
     * 注册服务订阅监听器，启动时打印本服务注册到 Nacos 的元数据。
     */
    @Bean
    @ConditionalOnMissingBean(NacosServiceSubscriber.class)
    public NacosServiceSubscriber nacosServiceSubscriber() {
        logger.info("注册 Nacos 服务订阅监听器");
        return new NacosServiceSubscriber();
    }
}
