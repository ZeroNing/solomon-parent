package com.steven.solomon.service.impl;

import com.steven.solomon.annotation.RabbitMq;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service("delayedMQService")
public class DelayedMQService extends AbstractMQService {

  @Override
  public AbstractExchange initExchange(String exchangeName, RabbitMq rabbitMq) {
    Map<String, Object> props = new HashMap<>(1);
    //延迟交换器类型
    props.put("x-delayed-type", rabbitMq.exchangeTypes());
    return new CustomExchange(exchangeName, "x-delayed-message", true, false, props);
  }

  @Override
  public Binding initBinding(Queue queue, AbstractExchange exchange, String routingKey) {
    return BindingBuilder.bind(queue).to((Exchange) exchange).with(routingKey).noargs();

  }
}
