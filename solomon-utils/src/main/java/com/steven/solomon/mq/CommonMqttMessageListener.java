package com.steven.solomon.mq;

public interface CommonMqttMessageListener<T,R> {

    /**
     * 消费方法
     */
    R handleMessage(T body) throws Exception;
}
