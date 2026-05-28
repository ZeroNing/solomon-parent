package com.steven.solomon.pojo.entity;

import java.io.Serializable;

/**
 * 消息队列基础消息体，封装消费者数据、租户编码和消息ID。
 *
 * <p>所有 MQ 消息统一使用该对象包装，携带租户信息以支持多租户场景下的消息路由。</p>
 *
 * @param <T> 消息体（body）的业务数据类型
 */
public class BaseMq<T> implements Serializable {

    /**
     * 消费者数据
     */
    private T body;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 消息ID
     */
    private String msgId;

    /**
     * 获取消息ID。
     *
     * @return 消息ID
     */
    public String getMsgId() {
        return msgId;
    }

    /**
     * 设置消息ID。
     *
     * @param msgId 消息ID
     */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /**
     * 默认无参构造函数。
     */
    public BaseMq() {

    }

    /**
     * 使用消息体构造。
     *
     * @param body 消费者数据
     */
    public BaseMq(T body) {
        this.body = body;
    }

    /**
     * 获取消费者数据。
     *
     * @return 消费者数据
     */
    public T getBody() {
        return body;
    }

    /**
     * 设置消费者数据。
     *
     * @param body 消费者数据
     */
    public void setBody(T body) {
        this.body = body;
    }

    /**
     * 获取租户编码。
     *
     * @return 租户编码
     */
    public String getTenantCode() {
        return tenantCode;
    }

    /**
     * 设置租户编码。
     *
     * @param tenantCode 租户编码
     */
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
