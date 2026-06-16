package com.steven.handler;

import com.steven.solomon.rabbitmq.annotation.MessageListener;
import com.steven.solomon.rabbitmq.consumer.AbstractConsumer;
import com.steven.solomon.rabbitmq.entity.RabbitMqModel;
import com.steven.solomon.rabbitmq.utils.RabbitUtils;

@MessageListener(queues = "A",exchange = "A")
public class AHandler extends AbstractConsumer<String,String> {

    protected AHandler(RabbitUtils rabbitUtils) {
        super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("直连队列[A]收到的消息为:{}",body);
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> rabbitMqModel) {

    }
}
