package com.steven.solomon.code;

import com.steven.solomon.pojo.enums.BaseEnum;

public enum RabbitMqOverFlowEnum implements BaseEnum<String> {
    DROP_HEAD("drop-head","删除queue头部的消息"),
    REJECT_PUBLISH("reject-publish","最近发来的消息将被丢弃"),
    REJECT_PUBLISH_DLX("reject-publish-dlx","拒绝发送消息到死信交换器"),;

    private String label;

    private String desc;

    RabbitMqOverFlowEnum(String label,String desc) {
        this.label = label;
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public String key() {
        return this.name();
    }
}
