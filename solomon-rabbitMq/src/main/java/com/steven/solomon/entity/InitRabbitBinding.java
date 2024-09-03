package com.steven.solomon.entity;


import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.code.BaseRabbitMqCode;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InitRabbitBinding implements Serializable {
    /**
     * 队列名
     */
    String queue;
    /**
     * 交换器名
     */
    String exchange;
    /**
     * 路由键名
     */
    String routingKey;

    Map<String, Object> args;

    public InitRabbitBinding(MessageListener messageListener, String queueName, boolean isInitDlxMap, boolean isAddDlxPrefix) {
        // 队列名
        this.queue = this.getName(queueName, isAddDlxPrefix);
        // 交换机名
        this.exchange = this.getName(messageListener.exchange(), isAddDlxPrefix);
        // 路由
        this.routingKey = this.getName(messageListener.routingKey(), isAddDlxPrefix);
        this.args = this.initArgs(messageListener, isInitDlxMap);
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
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

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    private String getName(String name, boolean isAddDlxPrefix) {
        if (ValidateUtils.isEmpty(name)) {
            return name;
        }
        name = SpringUtil.getElValue(name,ValidateUtils.getElDefaultValue(name));
        return isAddDlxPrefix ? BaseRabbitMqCode.DLX_PREFIX + name : name;
    }

    /**
     * 绑定死信队列参数
     *
     * @param messageListener     MQ注解
     * @param isInitDlxMap 是否初始化死信队列参数
     * @return 死信队列参数
     */
    private Map<String, Object> initArgs(MessageListener messageListener, boolean isInitDlxMap) {
        boolean dlx = !void.class.equals(messageListener.dlxClazz()) || messageListener.delay() != 0L;
        Map<String, Object> args = new HashMap<>(3);
        if(messageListener.lazy()){
            args.put(BaseRabbitMqCode.QUEUE_MODE, BaseRabbitMqCode.QUEUE_LAZY);
        }
        if (!dlx || !isInitDlxMap) {
            return args;
        }
        args.put(BaseRabbitMqCode.DLX_EXCHANGE_KEY, BaseRabbitMqCode.DLX_PREFIX + messageListener.exchange());

        if (ValidateUtils.isNotEmpty(messageListener.routingKey())) {
            args.put(BaseRabbitMqCode.DLX_ROUTING_KEY, BaseRabbitMqCode.DLX_PREFIX + messageListener.routingKey());
        }
        /**
         * x-message-ttl 在创建队列时设置的消息TTL，表示消息在队列中最多能存活多久（ms）；
         * Expiration 发布消息时设置的消息TTL，消息自产生后的存活时间（ms）；
         * x-delay 由rabbitmq_delayed_message_exchange插件提供TTL，从交换机延迟投递到队列的时间（ms）；
         */
        if (messageListener.delay() != 0L && !messageListener.isDelayExchange()) {
            args.put(BaseRabbitMqCode.DLX_TTL, messageListener.delay());
        }
        return args;
    }
}
