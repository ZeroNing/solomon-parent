package com.steven.solomon.mq.model;

import java.io.Serializable;

/**
 * 消息队列基础消息体，封装消费者数据、租户编码和消息ID。
 *
 * <p>所有 MQ 消息统一使用该对象包装，携带租户信息以支持多租户场景下的消息路由。
 * 业务侧继承本类或直接使用泛型承载具体业务消息体。</p>
 *
 * @param <T> 消息体（body）的业务数据类型
 * @author steven
 */
public class BaseMq<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消费者数据（业务消息体）。 */
    private T body;

    /** 租户编码，用于多租户消息路由。 */
    private String tenantCode;

    /** 消息ID，用于幂等校验与链路追踪。 */
    private String msgId;

    /** 默认无参构造函数。 */
    public BaseMq() {
    }

    /** 使用消息体构造。 */
    public BaseMq(T body) {
        this.body = body;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
