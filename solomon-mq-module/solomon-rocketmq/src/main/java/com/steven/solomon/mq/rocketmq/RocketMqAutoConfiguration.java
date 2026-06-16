package com.steven.solomon.mq.rocketmq;

import com.steven.solomon.utils.logger.LoggerUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * RocketMQ 自动配置。
 *
 * <p>当 classpath 存在 {@link RocketMQTemplate} 且 {@code rocketmq.enabled=true}（默认开启）时生效，
 * 自动注册 {@link RocketMqSendService} 作为 {@link com.steven.solomon.mq.SendService} 的默认实现。</p>
 *
 * @author steven
 */
@AutoConfiguration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class RocketMqAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(RocketMqAutoConfiguration.class);

    /**
     * 注册 RocketMQ 发送服务，业务通过 {@link com.steven.solomon.mq.SendService} 注入即可。
     */
    @Bean
    @ConditionalOnMissingBean(name = "rocketMqSendService")
    public <T> RocketMqSendService<T> rocketMqSendService(RocketMQTemplate rocketMQTemplate) {
        logger.info("注册 RocketMQ 发送服务");
        return new RocketMqSendService<>(rocketMQTemplate);
    }
}
