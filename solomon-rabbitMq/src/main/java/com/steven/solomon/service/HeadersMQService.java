package com.steven.solomon.service;

import com.steven.solomon.annotation.RabbitMq;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.BindingBuilder.HeadersExchangeMapConfigurer;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service("headersMQService")
public class HeadersMQService extends AbstractMQService{

  @Override
  protected AbstractExchange initExchange(String exchange, RabbitMq rabbitMq) {
    return new HeadersExchange(exchange);
  }

  @Override
  protected Binding initBinding(Queue queue, AbstractExchange exchange, String routingKey,RabbitMq rabbitMq) {
    HeadersExchangeMapConfigurer headersExchangeMapConfigurer = BindingBuilder.bind(queue).to((HeadersExchange)exchange);
    String[] headers = rabbitMq.headers();
    Map<String,Object> headerMap = new HashMap<>();
    if (rabbitMq.matchValue()) {
      for (int i = 0; i <= headers.length; i++) {
        if(i!=0 && i%2==0){
          continue;
        }
        String key = headers[i];
        Object value = i+1> headers.length ? null : headers[i + 1];
        headerMap.put(key, value);
      }
    }
    if(rabbitMq.matchAll()){
      return rabbitMq.matchValue() ? headersExchangeMapConfigurer.whereAll(headerMap).match() : headersExchangeMapConfigurer.whereAll(headers).exist();
    } else {
      return rabbitMq.matchValue() ? headersExchangeMapConfigurer.whereAny(headerMap).match() : headersExchangeMapConfigurer.whereAny(headers).exist();
    }
  }
}
