package com.steven.solomon.service;

import com.steven.solomon.annotation.MessageListener;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMq延迟队列注册
 */
@Service("delayedMQService")
public class DelayedMQService extends AbstractMQService {

    @Override
    protected AbstractExchange initExchange(String exchangeName, MessageListener messageListener) {
        Map<String, Object> props = new HashMap<>(1);
        //延迟交换器类型
        props.put("x-delayed-type", messageListener.exchangeTypes());
        return new CustomExchange(exchangeName, "x-delayed-message", true, false, props);
    }

    @Override
    protected Binding initBinding(Queue queue, AbstractExchange exchange, String routingKey, MessageListener messageListener) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
    }
}
