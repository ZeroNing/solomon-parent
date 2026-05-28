package com.steven.solomon.entiy;

import com.steven.solomon.pojo.entity.BaseMq;

/**
 * Redis消息队列数据模型。
 *
 * <p>封装Redis消息队列的消息体，包含主题名、租户编码和消息内容。</p>
 *
 * @param <T> 消息体类型
 */
public class RedisQueueModel<T> extends BaseMq<T> {

    /**
     * 主题名
     */
    private String topic;

    /**
     * 默认构造函数。
     */
    public RedisQueueModel() {
        super();
    }

    /**
     * 带主题名和消息体的构造函数。
     * @param topic 主题名
     * @param body  消息体
     */
    public RedisQueueModel(String topic,T body) {
        this.topic = topic;
        setBody(body);
    }

    /**
     * 带主题名、租户编码和消息体的构造函数。
     * @param topic      主题名
     * @param tenantCode 租户编码
     * @param body       消息体
     */
    public RedisQueueModel(String topic,String tenantCode,T body) {
        this.topic = topic;
        setTenantCode(tenantCode);
        setBody(body);
    }

    /**
     * 获取主题名。
     * @return 主题名
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 设置主题名。
     * @param topic 主题名
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
