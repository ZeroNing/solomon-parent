package com.steven.solomon.service.impl;

import com.steven.solomon.annotation.RabbitMq;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;
/**
 * RabbitMq广播队列注册
 */
@Service("fanoutMQService")
public class FanoutMQService extends AbstractMQService {

	@Override
	protected AbstractExchange initExchange(String exchangeName, RabbitMq rabbitMq) {
		return new FanoutExchange(exchangeName);
	}

	@Override
	protected Binding initBinding(Queue queue, AbstractExchange exchange, String routingKey) {
		return BindingBuilder.bind(queue).to((FanoutExchange) exchange);
	}
}
