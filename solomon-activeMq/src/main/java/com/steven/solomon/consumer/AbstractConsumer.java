package com.steven.solomon.consumer;

import javax.jms.Message;
import javax.jms.MessageListener;

public class AbstractConsumer<T> implements MessageListener {

    @Override
    public void onMessage(Message message) {

    }
}
