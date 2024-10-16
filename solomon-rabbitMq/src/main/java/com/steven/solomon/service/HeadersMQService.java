package com.steven.solomon.service;

import com.steven.solomon.annotation.MessageListener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.BindingBuilder.HeadersExchangeMapConfigurer;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service("headersMQService")
public class HeadersMQService extends AbstractMQService<HeadersExchange> {

    @Override
    protected HeadersExchange initExchange(String exchange, MessageListener messageListener) {
        return new HeadersExchange(exchange);
    }

    @Override
    protected Binding initBinding(Queue queue, HeadersExchange exchange, String routingKey, MessageListener messageListener) {
        HeadersExchangeMapConfigurer headersExchangeMapConfigurer = BindingBuilder.bind(queue).to(exchange);
        String[] headers = messageListener.headers();
        Map<String, Object> headerMap = new HashMap<>();
        if (messageListener.matchValue()) {
            for (int i = 0; i < headers.length; i += 2) {
                String key = headers[i];
                Object value = i + 1 > headers.length ? null : headers[i + 1];
                headerMap.put(key, value);
            }
        }
        if (messageListener.matchAll()) {
            return messageListener.matchValue() ? headersExchangeMapConfigurer.whereAll(headerMap).match() : headersExchangeMapConfigurer.whereAll(headers).exist();
        } else {
            return messageListener.matchValue() ? headersExchangeMapConfigurer.whereAny(headerMap).match() : headersExchangeMapConfigurer.whereAny(headers).exist();
        }
    }
}
