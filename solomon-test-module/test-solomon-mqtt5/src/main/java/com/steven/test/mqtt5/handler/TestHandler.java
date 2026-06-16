package com.steven.test.mqtt5.handler;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.v5.consumer.AbstractConsumer;
import com.steven.solomon.mqtt.model.MqttMessageModel;

@MessageListener(topics = "top/+/123")
public class TestHandler extends AbstractConsumer<String,String> {

    @Override
    public String handleMessage(String body) throws Exception {
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttMessageModel<String> model) {

    }
}

