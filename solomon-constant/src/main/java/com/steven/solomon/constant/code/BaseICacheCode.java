package com.steven.solomon.constant.code;

public interface BaseICacheCode {
    /**
     * rabbitMq队列失败redis分组
     */
    String RABBIT_FAIL_GROUP = "rabbit:fail";
    /**
     * 分布式锁接口redis分组
     */
    String REDIS_LOCK = "lock";
    /**
     * rabbitMq队列锁
     */
    String RABBIT_LOCK = "rabbitMQ-correlationId-lock";
}
