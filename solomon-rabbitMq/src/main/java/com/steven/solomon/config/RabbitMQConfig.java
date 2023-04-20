package com.steven.solomon.config;

import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.profile.RabbitMQProfile;
import com.steven.solomon.service.AbstractRabbitCallBack;
import com.steven.solomon.spring.SpringUtil;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	private Logger logger = LoggerUtils.logger(RabbitMQConfig.class);

	private final RabbitMQProfile profile;

	public RabbitMQConfig(RabbitMQProfile profile) {this.profile = profile;}

	@Bean("cachingConnectionFactory")
	public CachingConnectionFactory cachingConnectionFactory(){
		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setUsername(profile.getUserName());
		factory.setPassword(profile.getPassword());
		factory.setHost(profile.getHost());
		factory.setPort(profile.getPort());
		return factory;
	}

	/**
	 * 接受数据自动的转换为Json
	 */
	@Bean("messageConverter")
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean("rabbitTemplate")
	public RabbitTemplate rabbitTemplate(CachingConnectionFactory cachingConnectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter());
		// 开启发送确认
		cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
		// 开启发送失败退回
		cachingConnectionFactory.setPublisherReturns(true);
		rabbitTemplate.setMandatory(true);
		rabbitTemplate.setMessageConverter(messageConverter());
		RabbitCallBack rabbitCallBack = new RabbitCallBack(SpringUtil.getBeansOfType(AbstractRabbitCallBack.class).values());
		rabbitTemplate.setConfirmCallback(rabbitCallBack);
		rabbitTemplate.setReturnsCallback(rabbitCallBack);
		return rabbitTemplate;
	}

	@Bean("rabbitAdmin")
	public RabbitAdmin rabbitAdmin(CachingConnectionFactory cachingConnectionFactory) {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(cachingConnectionFactory);
		logger.info("RabbitAdmin启动了。。。");
		// 设置启动spring容器时自动加载这个类(这个参数现在默认已经是true，可以不用设置)
		rabbitAdmin.setAutoStartup(true);
		return rabbitAdmin;
	}

}
