package com.steven.handler;

import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.utils.RabbitUtils;

@MessageListener(queues = "G",exchange = "E",routingKey = "G")
public class GHandler extends AbstractConsumer<String,String> {

    protected GHandler(RabbitUtils rabbitUtils) {
        super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("直连队列[G],并准备测试请求回应,收到的消息为:{}",body);
        return body;
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> rabbitMqModel) {

    }
}
