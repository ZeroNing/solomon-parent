package com.steven.solomon.config;

import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"springUtil"})
public class RabbitMQConfig {

	private Logger logger = LoggerUtils.logger(RabbitMQConfig.class);

	/**
	 * 接受数据自动的转换为Json
	 */
	@Bean("messageConverter")
	@ConditionalOnMissingBean(MessageConverter.class)
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean("rabbitTemplate")
	public RabbitTemplate rabbitTemplate(CachingConnectionFactory cachingConnectionFactory,MessageConverter messageConverter,
			RabbitProperties properties) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		rabbitTemplate.setMandatory(properties.getTemplate().getMandatory());
		if(ValidateUtils.isNotEmpty(properties.getTemplate().getReceiveTimeout())){
			rabbitTemplate.setReceiveTimeout(properties.getTemplate().getReceiveTimeout().toMillis());
		}
		if(ValidateUtils.isNotEmpty(properties.getTemplate().getReplyTimeout())){
			rabbitTemplate.setReplyTimeout(properties.getTemplate().getReplyTimeout().toMillis());
		}
		Map<String,AbstractRabbitCallBack> callBackMap = SpringUtil.getBeansOfType(AbstractRabbitCallBack.class);
		if(ValidateUtils.isNotEmpty(callBackMap)){
			RabbitCallBack                     rabbitCallBack = new RabbitCallBack(callBackMap.values());
			rabbitTemplate.setConfirmCallback(rabbitCallBack);
			rabbitTemplate.setReturnsCallback(rabbitCallBack);
		}
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
