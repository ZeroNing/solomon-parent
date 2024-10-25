package com.steven.handler;

import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.utils.RabbitUtils;
import org.springframework.amqp.core.ExchangeTypes;

@MessageListener(queues = "B",exchange = "B",exchangeTypes = ExchangeTypes.FANOUT)
public class BHandler extends AbstractConsumer<String,String> {

    protected BHandler(RabbitUtils rabbitUtils) {
        super(rabbitUtils);
    }

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("广播队列[B]收到的消息为:{}",body);
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<String> rabbitMqModel) {

    }
}
