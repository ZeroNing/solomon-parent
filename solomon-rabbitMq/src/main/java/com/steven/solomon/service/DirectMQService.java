package com.steven.solomon.service;

import com.steven.solomon.annotation.MessageListener;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

/**
 * RabbitMq直连队列注册
 */
@Service("directMQService")
public class DirectMQService extends AbstractMQService<DirectExchange> {

    @Override
    protected DirectExchange initExchange(String exchangeName, MessageListener messageListener) {
        return new DirectExchange(exchangeName);
    }

    @Override
    protected Binding initBinding(Queue queue, DirectExchange exchange, String routingKey, MessageListener messageListener) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

}
