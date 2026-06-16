package com.steven.test.rabbitmq.handler;

import com.steven.solomon.rabbitmq.annotation.MessageListener;
import com.steven.solomon.rabbitmq.consumer.AbstractConsumer;
import com.steven.solomon.rabbitmq.entity.RabbitMqModel;
import com.steven.solomon.rabbitmq.utils.RabbitUtils;
import org.springframework.amqp.core.ExchangeTypes;

@MessageListener(queues = "E",exchange = "C",routingKey = "*.TEST",exchangeTypes = ExchangeTypes.TOPIC)
public class EHandler extends AbstractConsumer<String,String> {

    protected EHandler(RabbitUtils rabbitUtils) {
        super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("主题队列[E]收到的消息为:{}",body);
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> rabbitMqModel) {

    }
}
