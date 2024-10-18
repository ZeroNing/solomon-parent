package com.steven.solomon.service;

import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.entity.InitRabbitBinding;
import com.steven.solomon.properties.RabbitMqProperties;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

public abstract class AbstractMQService<E extends AbstractExchange> {

    public static String SERVICE_NAME = "MQService";

    public Queue initBinding(RabbitMqProperties properties, MessageListener messageListener, String queueName, RabbitAdmin admin, boolean isInitDlxMap, boolean isAddDlxPrefix) {
        InitRabbitBinding initRabbitBinding = new InitRabbitBinding(properties,messageListener,admin, queueName, isInitDlxMap, isAddDlxPrefix);
        // 初始化队列
        Queue queue = initRabbitBinding.getQueue();
        // 绑定队列
        admin.declareQueue(queue);
        // 绑定交换机
        E exchange = initExchange(initRabbitBinding.getExchange(), messageListener);
        admin.declareExchange(exchange);
        // 绑定
        admin.declareBinding(this.initBinding(queue, exchange, initRabbitBinding.getRoutingKey(), messageListener));
        return queue;
    }

    /**
     * 初始化交换机
     */
    protected abstract E initExchange(String exchange, MessageListener messageListener);

    /**
     * 初始化绑定
     */
    protected abstract Binding initBinding(Queue queue, E exchange, String routingKey, MessageListener messageListener);
}
