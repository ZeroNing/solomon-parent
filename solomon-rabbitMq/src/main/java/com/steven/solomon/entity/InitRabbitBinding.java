package com.steven.solomon.entity;


import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.code.BaseRabbitMqCode;
import com.steven.solomon.properties.RabbitMqProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

import java.io.Serializable;

public class InitRabbitBinding implements Serializable {
    /**
     * 队列名
     */
    String queueName;
    /**
     * 交换器名
     */
    String exchange;
    /**
     * 路由键名
     */
    String routingKey;

    Queue queue;

    public InitRabbitBinding(RabbitMqProperties properties, MessageListener messageListener, RabbitAdmin admin, String queueName, boolean isInitDlxMap, boolean isAddDlxPrefix) {
        // 队列名
        this.queueName = this.getName(queueName, isAddDlxPrefix);
        // 交换机名
        this.exchange = this.getName(messageListener.exchange(), isAddDlxPrefix);
        // 路由
        this.routingKey = this.getName(messageListener.routingKey(), isAddDlxPrefix);
        //初始化队列
        this.queue = this.initQueue(properties,messageListener,admin,isInitDlxMap);
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    private String getName(String name, boolean isAddDlxPrefix) {
        if (ValidateUtils.isEmpty(name)) {
            return name;
        }
        name = SpringUtil.getElValue(name,ValidateUtils.getElDefaultValue(name));
        return isAddDlxPrefix ? BaseRabbitMqCode.DLX_PREFIX + name : name;
    }

    private Queue initQueue(RabbitMqProperties properties,MessageListener messageListener, RabbitAdmin admin, boolean isInitDlxMap){
        if(ValidateUtils.isNotEmpty(properties) && properties.getAutoDeleteQueue()){
            admin.deleteQueue(queueName);
            admin.deleteExchange(exchange);
        }
        boolean dlx = !void.class.equals(messageListener.dlxClazz()) || messageListener.ttl() != 0;
        QueueBuilder queueBuilder = messageListener.isPersistence() ? QueueBuilder.durable(queueName) : QueueBuilder.nonDurable(queueName);
        if(messageListener.lazy()){
            queueBuilder.lazy();
        }
        queueBuilder.overflow(messageListener.queueOverflow());
        if (!dlx || !isInitDlxMap) {
            return queueBuilder.build();
        }
        queueBuilder.deadLetterExchange(BaseRabbitMqCode.DLX_PREFIX + messageListener.exchange());
        if (ValidateUtils.isNotEmpty(messageListener.routingKey())) {
            queueBuilder.deadLetterRoutingKey(BaseRabbitMqCode.DLX_PREFIX + messageListener.routingKey());
        }
        if (messageListener.ttl() != 0L && !messageListener.isDelayExchange()) {
            queueBuilder.ttl(messageListener.ttl());
        }
        if(!ValidateUtils.equals(messageListener.queueMaxLength(),-1)){
            queueBuilder.maxLength(messageListener.queueMaxLength());
        }
        if(!ValidateUtils.equals(messageListener.queueMaxLengthByte(),-1)){
            queueBuilder.maxLengthBytes(messageListener.queueMaxLengthByte());
        }
        if(!ValidateUtils.equals(messageListener.maxPriority(),-1)){
            queueBuilder.maxPriority(messageListener.maxPriority());
        }
        return queueBuilder.build();
    }
}
