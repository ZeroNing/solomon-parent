package com.steven.solomon.service.impl;

import com.steven.solomon.annotation.RabbitMq;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.BindingBuilder.DestinationConfigurer;
import org.springframework.amqp.core.BindingBuilder.GenericArgumentsConfigurer;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service("directMQService")
public class DirectMQService extends AbstractMQService {

	@Override
	public AbstractExchange initExchange(String exchangeName, RabbitMq rabbitMq) {
		return new DirectExchange(exchangeName);
	}

	@Override
	public Binding initBinding(Queue queue,AbstractExchange exchange,String routingKey) {
		return BindingBuilder.bind(queue).to((DirectExchange) exchange).with(routingKey);
	}

}
