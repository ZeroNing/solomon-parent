package com.steven.solomon.rabbitmq.config;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;

import java.util.Collection;

public class RabbitCallBack implements ReturnsCallback, ConfirmCallback {

    public final Logger logger = LoggerUtils.logger(RabbitCallBack.class);

    private static Collection<AbstractRabbitCallBack> rabbitCallBackList;

    public RabbitCallBack(Collection<AbstractRabbitCallBack> rabbitCallBackList) {
        RabbitCallBack.rabbitCallBackList = rabbitCallBackList;
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (!ack) {
            logger.error("RabbitMQConfig:消息发送失败:correlationData({}),ack(false),cause({})", correlationData, cause);
        }
        if (ObjectUtil.isNotEmpty(RabbitCallBack.rabbitCallBackList)) {
            for (AbstractRabbitCallBack abstractRabbitCallBack : RabbitCallBack.rabbitCallBackList) {
                abstractRabbitCallBack.saveRabbitCallBack(correlationData, ack, cause);
            }
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        logger.error("RabbitMQConfig:消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText(), returned.getMessage());
        if (ObjectUtil.isNotEmpty(RabbitCallBack.rabbitCallBackList)) {
            for (AbstractRabbitCallBack abstractRabbitCallBack : RabbitCallBack.rabbitCallBackList) {
                abstractRabbitCallBack.saveReturnedMessage(returned);
            }
        }
    }
}
