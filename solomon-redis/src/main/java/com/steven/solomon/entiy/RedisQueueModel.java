package com.steven.solomon.entiy;

import com.steven.solomon.pojo.entity.BaseMq;

public class RedisQueueModel<T> extends BaseMq<T> {

    /**
     * 主题名
     */
    private String topic;

    public RedisQueueModel() {
        super();
    }

    public RedisQueueModel(String topic,T body) {
        this.topic = topic;
        setBody(body);
    }

    public RedisQueueModel(String topic,String tenantCode,T body) {
        this.topic = topic;
        setTenantCode(tenantCode);
        setBody(body);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
