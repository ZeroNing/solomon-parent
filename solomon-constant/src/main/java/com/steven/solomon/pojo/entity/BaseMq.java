package com.steven.solomon.pojo.entity;

import java.io.Serializable;

public class BaseMq<T> implements Serializable {

    /**
     * 消费者数据
     */
    private              T body;

    public BaseMq(){

    }

    public BaseMq(T body){
        this.body = body;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
