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
import org.eclipse.paho.client.mqttv3.*;
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
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> clazzList) throws Exception {
        String url = mqttProfile.getUrl().split(",")[0];
        MqttClient mqttClient = new MqttClient(url, ValidateUtils.getOrDefault(mqttProfile.getClientId(), UUID.randomUUID().toString()));
        MqttConnectOptions options = utils.initMqttConnectOptions(mqttProfile);
        mqttClient.connect(options);
        utils.putOptionsMap(tenantCode,options);
        // 订阅主题
        utils.subscribe(mqttClient,clazzList);

        //配置callback
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                logger.info("租户:{} 重连{}",tenantCode,reconnect ? "成功" : "失败");
                if(reconnect){
                    for (Object abstractConsumer : clazzList) {
                        MessageListener messageListener = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), MessageListener.class);
                        if(ValidateUtils.isEmpty(messageListener) || ValidateUtils.isEmpty(messageListener.topics())){
                            continue;
                        }
                        try {
                            for(String topic : messageListener.topics()){
                                logger.info("租户:{} 重新订阅[{}]主题",tenantCode,topic);
                                mqttClient.subscribe(topic, messageListener.qos(), (IMqttMessageListener) BeanUtil.copyProperties(abstractConsumer,abstractConsumer.getClass(), (String) null));
                            }
                        } catch (MqttException e) {
                            logger.error("重连重新订阅主题失败,异常为:",e);
                        }
                    }
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.info("租户:{} 断开连接,异常为:",tenantCode,cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        //保存client
        utils.putClient(tenantCode,mqttClient);
    }

    @Override
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception {
        this.initMqttClient(tenantCode,mqttProfile,new ArrayList<>(SpringUtil.getBeansWithAnnotation(MessageListener.class).values()));
    }
}
