package com.steven.solomon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.service.MqttInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultMqttInitService implements MqttInitService {

    private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

    private final MqttUtils utils;

    public DefaultMqttInitService(MqttUtils utils) {
        this.utils = utils;
    }

    @Override
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception {
        initMqttClient(tenantCode, mqttProfile,new ArrayList<>(SpringUtil.getBeansWithAnnotation(MessageListener.class).values()));
    }

    @Override
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> clazzList) throws Exception {
        String url = mqttProfile.getUrl().split(",")[0];
        MqttClient client = new MqttClient(url, ValidateUtils.getOrDefault(mqttProfile.getClientId(), UUID.randomUUID().toString()));
        MqttConnectionOptions options = utils.initMqttConnectOptions(mqttProfile);
        client.connect(options);
        utils.putOptionsMap(tenantCode,options);
        client.setCallback(new MqttCallback() {
            @Override
            public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
                logger.error("客户端与服务器断开连接,原因为：",mqttDisconnectResponse.getException());
            }

            @Override
            public void mqttErrorOccurred(MqttException e) {
                logger.error("运行错误,原因为：",e);
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttToken iMqttToken) {

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                logger.info("租户:{} 重连{}",tenantCode,reconnect ? "成功" : "失败");
                if(reconnect){
                    for (Object abstractConsumer : clazzList) {
                        MessageListener messageListener = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), MessageListener.class);
                        if (ValidateUtils.isNotEmpty(messageListener)) {
                            try {
                                for(String topic : messageListener.topics()){
                                    logger.info("租户:{} 重新订阅[{}]主题",tenantCode,topic);
                                    client.subscribe(new MqttSubscription[]{new MqttSubscription(topic, messageListener.qos())}, new IMqttMessageListener[]{(IMqttMessageListener) BeanUtil.copyProperties(abstractConsumer,abstractConsumer.getClass())});
                                }
                            } catch (MqttException e) {
                                logger.error("重连重新订阅主题失败,异常为:",e);
                            }
                        }
                    }
                }
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {

            }
        });
        // 订阅主题
        utils.subscribe(client,clazzList);
        //保存client
        utils.putClient(tenantCode,client);
    }
}
