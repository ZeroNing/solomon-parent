package com.steven.handler;

import com.steven.solomon.rabbitmq.annotation.DlxMessageListener;
import com.steven.solomon.rabbitmq.annotation.MessageListener;
import com.steven.solomon.rabbitmq.consumer.AbstractConsumer;
import com.steven.solomon.rabbitmq.entity.RabbitMqModel;
import com.steven.solomon.rabbitmq.utils.RabbitUtils;
import org.springframework.stereotype.Component;

@DlxMessageListener
public class DlxFHandler extends AbstractConsumer<String,String> {

    protected DlxFHandler(RabbitUtils rabbitUtils) {
        super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("死信队列[F],收到的消息为:{}",body);
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> rabbitMqModel) {

    }
}
