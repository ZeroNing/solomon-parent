package com.steven.solomon.mqtt.mica.service;

import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.mqtt.service.MqttClientInitService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.mqtt.mica.utils.MqttUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.dromara.mica.mqtt.core.client.MqttClient;
import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.slf4j.Logger;

import java.util.List;

/**
 * Mica MQTT 默认客户端初始化服务。
 *
 * <p>基于 Mica MQTT 客户端实现，负责创建客户端连接并自动订阅监听器。</p>
 */
public class DefaultMqttInitService implements MqttClientInitService<MqttClientProperties> {

    /** 日志记录器 */
    private final Logger logger = LoggerUtils.logger(DefaultMqttInitService.class);

    /** Mica MQTT 工具类，负责客户端创建与消息发送 */
    private final MqttUtils utils;

    /**
     * 构造方法。
     *
     * @param utils Mica MQTT 工具类实例
     */
    public DefaultMqttInitService(MqttUtils utils) {
        this.utils = utils;
    }

    /**
     * 初始化指定租户的 Mica MQTT 客户端（自动扫描监听器）。
     *
     * @param tenantCode 租户编码
     * @param mqttProfile Mica MQTT 客户端配置
     */
    @Override
    public void initMqttClient(String tenantCode, MqttClientProperties mqttProfile) throws Exception {
        initMqttClient(tenantCode, mqttProfile, SpringUtil.getBeanListWithAnnotation(MessageListener.class));
    }

    /**
     * 初始化指定租户的 Mica MQTT 客户端并订阅监听器。
     *
     * @param tenantCode 租户编码
     * @param mqttProfile Mica MQTT 客户端配置
     * @param clazzList 监听器实例列表
     */
    @Override
    public void initMqttClient(String tenantCode, MqttClientProperties mqttProfile, List<Object> clazzList) throws Exception {
        // 创建 MQTT 客户端
        MqttClient mqttClient = utils.createMqttClient(tenantCode, mqttProfile);
        
        // 订阅主题
        utils.subscribe(mqttClient, clazzList, tenantCode);
        
        logger.info("MQTT客户端初始化成功，租户: {}, URL: {}", tenantCode, mqttProfile.getIp() + ":" + mqttProfile.getPort());
    }
}
