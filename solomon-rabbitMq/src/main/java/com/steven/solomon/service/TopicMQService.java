package com.steven.solomon.service;

import com.steven.solomon.annotation.MessageListener;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

/**
 * RabbitMq主题队列注册
 */
@Service("topicMQService")
public class TopicMQService extends AbstractMQService {

    @Override
    protected AbstractExchange initExchange(String exchangeName, MessageListener messageListener) {
        return new TopicExchange(exchangeName);
    }

    @Override
    protected Binding initBinding(Queue queue, AbstractExchange exchange, String routingKey, MessageListener messageListener) {
        return BindingBuilder.bind(queue).to((TopicExchange) exchange).with(routingKey);
    }
}
