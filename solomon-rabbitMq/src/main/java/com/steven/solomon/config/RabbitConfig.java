package com.steven.solomon.config;

import com.steven.solomon.service.*;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.RabbitUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(value = {RabbitProperties.class})
@Import(value = {RabbitUtils.class, DelayedMQService.class, DirectMQService.class, FanoutMQService.class, TopicMQService.class, HeadersMQService.class})
public class RabbitConfig {
    private final Logger logger = LoggerUtils.logger(RabbitConfig.class);

    public RabbitConfig(ApplicationContext applicationContext) {
        SpringUtil.setContext(applicationContext);
    }

    /**
     * 接受数据自动的转换为Json
     */
    @Bean("messageConverter")
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("rabbitTemplate")
    @ConditionalOnMissingBean(RabbitTemplate.class)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter,
                                         RabbitProperties properties) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(ValidateUtils.getOrDefault(properties.getTemplate().getMandatory(), true));
        if (ValidateUtils.isNotEmpty(properties.getTemplate().getReceiveTimeout())) {
            rabbitTemplate.setReceiveTimeout(properties.getTemplate().getReceiveTimeout().toMillis());
        }
        if (ValidateUtils.isNotEmpty(properties.getTemplate().getReplyTimeout())) {
            rabbitTemplate.setReplyTimeout(properties.getTemplate().getReplyTimeout().toMillis());
        }
        Map<String, AbstractRabbitCallBack> callBackMap = SpringUtil.getBeansOfType(AbstractRabbitCallBack.class);
        if (ValidateUtils.isNotEmpty(callBackMap)) {
            RabbitCallBack rabbitCallBack = new RabbitCallBack(callBackMap.values());
            rabbitTemplate.setConfirmCallback(rabbitCallBack);
            rabbitTemplate.setReturnsCallback(rabbitCallBack);
        }
        return rabbitTemplate;
    }

    @Bean("rabbitAdmin")
    @ConditionalOnMissingBean(RabbitAdmin.class)
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        logger.debug("RabbitAdmin启动了。。。");
        // 设置启动spring容器时自动加载这个类(这个参数现在默认已经是true，可以不用设置)
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

}
