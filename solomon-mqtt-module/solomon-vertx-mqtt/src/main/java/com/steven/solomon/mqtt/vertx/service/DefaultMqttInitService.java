package com.steven.solomon.mqtt.vertx.service;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.lang.UUID;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.mqtt.vertx.profile.MqttProfile;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.mqtt.vertx.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.mqtt.annotation.MessageListener;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.slf4j.Logger;

/**
 * Vert.x MQTT 默认客户端初始化服务。
 *
 * <p>基于 Vert.x MqttClient 实现，负责创建 Vertx 实例、建立 MQTT 连接、注册断开重连回调并自动订阅监听器。</p>
 */
public class DefaultMqttInitService implements MqttClientInitService<MqttProfile> {

    /** 日志记录器 */
    private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

    /** MQTT 工具类，负责连接参数构建、客户端注册与消息发送 */
    private final MqttUtils utils;

    /**
     * 构造方法。
     *
     * @param utils MQTT 工具类实例
     */
    public DefaultMqttInitService(MqttUtils utils) {
        this.utils = utils;
    }

    /**
     * 初始化指定租户的 Vert.x MQTT 客户端并订阅监听器。
     *
     * @param tenantCode 租户编码
     * @param mqttProfile MQTT 客户端配置
     * @param clazzList 监听器实例列表
     */
    @Override
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile, List<Object> clazzList) throws Exception {
        // 初始化 Vert.x
        Vertx vertx = utils.initVertx(mqttProfile.getVertx());
        utils.putVertx(tenantCode, vertx);

        // 从配置 URL 中获取第一个地址 (支持集群配置，逗号分隔)
        String url = mqttProfile.getUrl().split(",")[0];
        // 解析 host 和 port
        boolean ssl = url.startsWith("ssl://");
        String[] parts = url.replace("tcp://", "").replace("ssl://", "").split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : (ssl ? 8883 : 1883);

        // 初始化 MQTT 连接配置
        MqttClientOptions options = utils.initMqttConnectOptions(mqttProfile);
        // 设置 clientId
        String clientId = ObjectUtil.defaultIfNull(mqttProfile.getClientId(), UUID.randomUUID().toString());
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

    /**
     * 初始化指定租户的 Vert.x MQTT 客户端（自动扫描监听器）。
     *
     * @param tenantCode 租户编码
     * @param mqttProfile MQTT 客户端配置
     */
    @Override
    public void initMqttClient(String tenantCode, MqttProfile mqttProfile) throws Exception {
        this.initMqttClient(tenantCode, mqttProfile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
    }
}
