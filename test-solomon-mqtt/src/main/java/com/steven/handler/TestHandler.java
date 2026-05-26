package com.steven.handler;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.mqtt.model.MqttMessageModel;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@MessageListener(topics = "top/+/123",tenantRange = "test12")
public class TestHandler extends AbstractConsumer<String,String> {

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("接受的主题是:{},内容是:{}",topic,body);
        return "";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttMessageModel<String> model) {

    }
}
