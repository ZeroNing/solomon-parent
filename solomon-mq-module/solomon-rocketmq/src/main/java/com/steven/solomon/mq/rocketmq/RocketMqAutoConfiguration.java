package com.steven.solomon.mq.rocketmq;

import com.steven.solomon.utils.logger.LoggerUtils;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class RocketMqAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(RocketMqAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "rocketMqSendService")
    public <T> RocketMqSendService<T> rocketMqSendService(
            RocketMQTemplate rocketMQTemplate,
            ObjectProvider<MeterRegistry> meterRegistry) {
        logger.info("Register RocketMQ send service.");
        return new RocketMqSendService<>(rocketMQTemplate, meterRegistry.getIfAvailable());
    }

    @Bean("rocketMqHealthIndicator")
    @ConditionalOnMissingBean(name = "rocketMqHealthIndicator")
    public HealthIndicator rocketMqHealthIndicator(RocketMQTemplate rocketMQTemplate) {
        return new RocketMqHealthIndicator(rocketMQTemplate);
    }
}
