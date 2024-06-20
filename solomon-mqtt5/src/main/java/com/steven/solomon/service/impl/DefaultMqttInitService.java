package com.steven.solomon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.steven.solomon.annotation.Mqtt;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.service.MqttInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
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
        MqttClient client = new MqttClient(mqttProfile.getUrl(), ValidateUtils.getOrDefault(mqttProfile.getClientId(), UUID.randomUUID().toString()));
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
                    List<Object> clazzList = new ArrayList<>(SpringUtil.getBeansWithAnnotation(Mqtt.class).values());
                    for (Object abstractConsumer : clazzList) {
                        Mqtt mqtt = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), Mqtt.class);
                        if (ValidateUtils.isNotEmpty(mqtt)) {
                            try {
                                for(String topic : mqtt.topics()){
                                    logger.info("租户:{} 重新订阅[{}]主题",tenantCode,topic);
                                    client.subscribe(topic, mqtt.qos(), (IMqttMessageListener) BeanUtil.copyProperties(abstractConsumer,abstractConsumer.getClass(), (String) null));
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
        utils.subscribe(client);
        //保存client
        utils.putClient(tenantCode,client);
    }
}
