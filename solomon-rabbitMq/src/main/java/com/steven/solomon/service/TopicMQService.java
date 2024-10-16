package com.steven.solomon.service;

import com.steven.solomon.annotation.MessageListener;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

/**
 * RabbitMq主题队列注册
 */
@Service("topicMQService")
public class TopicMQService extends AbstractMQService<TopicExchange> {

    @Override
    protected TopicExchange initExchange(String exchangeName, MessageListener messageListener) {
        return new TopicExchange(exchangeName);
    }

    @Override
    protected Binding initBinding(Queue queue, TopicExchange exchange, String routingKey, MessageListener messageListener) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
