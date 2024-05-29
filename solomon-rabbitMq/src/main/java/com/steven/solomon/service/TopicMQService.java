package com.steven.solomon.service;

import com.steven.solomon.annotation.RabbitMq;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMq主题队列注册
 */
@Service("topicMQService")
public class TopicMQService extends AbstractMQService {

    @Override
    protected AbstractExchange initExchange(String exchangeName, RabbitMq rabbitMq) {
        return new TopicExchange(exchangeName);
    }

    @Override
    protected Binding initBinding(Queue queue, AbstractExchange exchange, String routingKey, RabbitMq rabbitMq) {
        return BindingBuilder.bind(queue).to((TopicExchange) exchange).with(routingKey);
    }
}
