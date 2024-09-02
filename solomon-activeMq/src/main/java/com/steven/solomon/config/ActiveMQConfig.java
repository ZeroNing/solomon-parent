package com.steven.solomon.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;

@Component
@Order(2)
@DependsOn("springUtil")
public class ActiveMQConfig {

    @Bean("jmsTemplate")
    @ConditionalOnMissingBean(JmsTemplate.class)
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter, ActiveMQProperties properties) {
        final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setMessageConverter(messageConverter);
        return jmsTemplate;
    }

    @Bean("messageConverter")
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter messageConverter() {
        return new MappingJackson2MessageConverter();
    }
}
