package com.steven.solomon.service.impl;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.service.MqttInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.slf4j.Logger;

public class DefaultMqttInitService implements MqttInitService {

    private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

    private final MqttUtils utils;

    public DefaultMqttInitService(MqttUtils utils) {
        this.utils = utils;
    }

    @Override
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> clazzList) throws Exception {
        // 初始化 Vert.x
        Vertx vertx = utils.initVertx(mqttProfile.getVertx());
        utils.putVertx(tenantCode, vertx);

        // 从配置 URL 中获取第一个地址 (支持集群配置，逗号分隔)
        String url = mqttProfile.getUrl().split(",")[0];
        // 解析 host 和 port
        String[] parts = url.replace("tcp://", "").replace("ssl://", "").split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 1883;

        // 初始化 MQTT 连接配置
        MqttClientOptions options = utils.initMqttConnectOptions(mqttProfile);
        // 设置 clientId
        String clientId = ValidateUtils.getOrDefault(mqttProfile.getClientId(), UUID.randomUUID().toString());
        options.setClientId(clientId);

        // 创建 MQTT Client
        MqttClient mqttClient = MqttClient.create(vertx, options);
        // 保存配置到 Map 中，key=租户编码
        utils.putOptionsMap(tenantCode, options);

        // 连接 MQTT Broker
        mqttClient.connect(port, host).onComplete(ar -> {
            if (ar.succeeded()) {
                logger.info("租户:{} MQTT 连接成功，clientId:{}", tenantCode, clientId);
                // 连接成功后订阅主题
                try {
                    utils.subscribe(mqttClient, clazzList, tenantCode);
                } catch (Exception e) {
                    logger.error("租户:{} 订阅主题失败", tenantCode, e);
                }
            } else {
                logger.error("租户:{} MQTT 连接失败", tenantCode, ar.cause());
            }
        });
        // 配置断开连接回调 - Vert.x 5.x 没有 reconnect 方法，需要手动重连
        mqttClient.closeHandler(v -> {
            logger.info("租户:{} MQTT 连接断开", tenantCode);
            mqttClient.connect(port, host).onComplete(ar -> {
                if (ar.succeeded()) {
                    logger.info("租户:{} MQTT 连接成功，clientId:{}", tenantCode, clientId);
                    // 连接成功后订阅主题
                    try {
                        utils.subscribe(mqttClient, clazzList, tenantCode);
                    } catch (Exception e) {
                        logger.error("租户:{} 订阅主题失败", tenantCode, e);
                    }
                } else {
                    logger.error("租户:{} MQTT 连接失败", tenantCode, ar.cause());
                }
            });
        });

        // 保存 client 到 Map 中，key=租户编码
        utils.putClient(tenantCode, mqttClient);
    }

    @Override
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception {
        this.initMqttClient(tenantCode, mqttProfile, new ArrayList<>(SpringUtil.getBeansWithAnnotation(MessageListener.class).values()));
    }
}
