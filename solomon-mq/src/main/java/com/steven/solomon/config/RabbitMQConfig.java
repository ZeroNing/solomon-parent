package com.steven.solomon.config;

import com.steven.solomon.condition.RabbitCondition;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.profile.BaseMQProfile;
import com.steven.solomon.service.AbstractRabbitCallBack;
import com.steven.solomon.spring.SpringUtil;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	private Logger logger = LoggerUtils.logger(RabbitMQConfig.class);

	@Autowired
	private BaseMQProfile mqProfile;

	@Bean("cachingConnectionFactory")
	@Conditional(value = RabbitCondition.class)
	public CachingConnectionFactory cachingConnectionFactory(){
		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setUsername(mqProfile.getUserName());
		factory.setPassword(mqProfile.getPassword());
		factory.setHost(mqProfile.getHost());
		factory.setPort(mqProfile.getPort());
		return factory;
	}

	/**
	 * 接受数据自动的转换为Json
	 */
	@Bean("messageConverter")
	@Conditional(value = RabbitCondition.class)
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean("rabbitTemplate")
	@Conditional(value = RabbitCondition.class)
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
	@Conditional(value = RabbitCondition.class)
	public RabbitAdmin rabbitAdmin(CachingConnectionFactory cachingConnectionFactory) {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(cachingConnectionFactory);
		logger.info("RabbitAdmin启动了。。。");
		// 设置启动spring容器时自动加载这个类(这个参数现在默认已经是true，可以不用设置)
		rabbitAdmin.setAutoStartup(true);
		return rabbitAdmin;
	}

}
