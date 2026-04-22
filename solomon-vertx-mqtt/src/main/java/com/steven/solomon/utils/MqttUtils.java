package com.steven.solomon.utils;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.code.MqttErrorCode;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.profile.MqttProfile;
import com.steven.solomon.profile.MqttProfile.MqttWill;
import com.steven.solomon.service.SendService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.messages.MqttPublishMessage;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttUtils implements SendService<MqttModel<?>> {

    private final Logger logger = LoggerUtils.logger(MqttUtils.class);

    private final Map<String, MqttClient> clientMap = new HashMap<>();

    private final Map<String, MqttClientOptions> optionsMap = new HashMap<>();

    private final Map<String, Vertx> vertxMap = new HashMap<>();

    public Map<String, MqttClientOptions> getOptionsMap() {
        return optionsMap;
    }

    public void putOptionsMap(String tenantCode, MqttClientOptions options) {
        this.optionsMap.put(tenantCode, options);
    }

    public Map<String, MqttClient> getClientMap() {
        return clientMap;
    }

    public void putClient(String tenantCode, MqttClient client) {
        this.clientMap.put(tenantCode, client);
    }

    public Map<String, Vertx> getVertxMap() {
        return vertxMap;
    }

    public void putVertx(String tenantCode, Vertx vertx) {
        this.vertxMap.put(tenantCode, vertx);
    }

    /**
     * 发送消息
     *
     * @param data 消息内容
     */
    @Override
    public void send(MqttModel<?> data) throws Exception {
        try {
            MqttClient client = getClient(data.getTenantCode());
            String json = JSONUtil.toJsonStr(data);
            client.publish(data.getTopic(), 
                    Buffer.buffer(json.getBytes(StandardCharsets.UTF_8)),
                    MqttQoS.valueOf(data.getQos()),
                    data.getRetained(),
                    false);
        } catch (Exception e) {
            logger.error(String.format("MQTT: 主题[%s]发送消息失败", data.getTopic()), e);
            throw e;
        }
    }

    @Override
    public void sendDelay(MqttModel<?> data, long delay) throws Exception {
        send(data);
    }

    @Override
    public void sendExpiration(MqttModel<?> data, long expiration) throws Exception {
        send(data);
    }

    /**
     * 订阅消息
     *
     * @param tenantCode 租户编码
     * @param topic      主题
     * @param qos        消息质量
     * @param consumer   消费者
     */
    public void subscribe(String tenantCode, String topic, int qos, AbstractConsumer<?, ?> consumer) throws BaseException {
        if (ValidateUtils.isEmpty(topic)) {
            return;
        }
        MqttClient client = getClient(tenantCode);
        client.subscribe(topic, qos);
        // Vert.x MQTT 通过 publishHandler 接收消息
        client.publishHandler(message -> {
            String msgTopic = message.topicName();
            if (msgTopic.equals(topic)) {
                try {
                    consumer.messageArrived(msgTopic, message);
                } catch (Exception e) {
                    logger.error("消费消息异常:", e);
                }
            }
        });
    }

    /**
     * 订阅消息
     *
     * @param client     mqtt连接
     * @param tenantCode 租户编码
     */
    public void subscribe(MqttClient client, String tenantCode) throws Exception {
        List<Object> clazzList = new ArrayList<>(SpringUtil.getBeansWithAnnotation(MessageListener.class).values());
        this.subscribe(client, clazzList, tenantCode);
    }

    /**
     * 订阅消息
     *
     * @param client     mqtt连接
     * @param clazzList  消费者列表
     * @param tenantCode 租户编码
     */
    public void subscribe(MqttClient client, List<Object> clazzList, String tenantCode) throws Exception {
        if (ValidateUtils.isNotEmpty(clazzList)) {
            for (Object abstractConsumer : clazzList) {
                MessageListener messageListener = AnnotationUtil.getAnnotation(abstractConsumer.getClass(), MessageListener.class);
                if (ValidateUtils.isEmpty(messageListener) || ValidateUtils.isEmpty(messageListener.topics())) {
                    continue;
                }
                List<String> rangeList = Lambda.toList(Arrays.asList(messageListener.tenantRange()), ValidateUtils::isNotEmpty, key -> key);
                if (ValidateUtils.isEmpty(rangeList) || rangeList.contains(tenantCode)) {
                    for (String topic : messageListener.topics()) {
                        topic = SpringUtil.getElValue(topic);
                        AbstractConsumer<?, ?> consumer = (AbstractConsumer<?, ?>) BeanUtil.copyProperties(abstractConsumer, abstractConsumer.getClass(), (String) null);
                        // 订阅主题
                        client.subscribe(topic, messageListener.qos());
                        // 设置消息处理器
                        String finalTopic = topic;
                        client.publishHandler(message -> {
                            if (StrUtil.equalsIgnoreCase(message.topicName(), finalTopic)) {
                                try {
                                    consumer.messageArrived(finalTopic, message);
                                } catch (Exception e) {
                                    logger.error("消费消息异常,topic:{}", finalTopic, e);
                                }
                            }
                        });
                    }
                } else {
                    logger.info("{}租户,{}只支持{}范围", tenantCode, abstractConsumer.getClass().getSimpleName(), rangeList.toArray());
                }
            }
        }
    }

    /**
     * 取消订阅
     *
     * @param topic 主题
     */
    public void unsubscribe(String tenantCode, String[] topic) throws Exception {
        if (ValidateUtils.isEmpty(topic)) {
            return;
        }
        MqttClient client = getClient(tenantCode);
        for (String t : topic) {
            client.unsubscribe(t);
        }
    }

    /**
     * 关闭连接
     */
    public void disconnect(String tenantCode) throws Exception {
        MqttClient client = getClient(tenantCode);
        client.disconnect();
        Vertx vertx = vertxMap.get(tenantCode);
        if (vertx != null) {
            vertx.close();
        }
    }

    /**
     * 重新连接 - Vert.x 5.x 没有内置 reconnect 方法，需要重新创建连接
     */
    public void reconnect(String tenantCode) throws Exception {
        // Vert.x 5.x 没有 reconnect 方法，需要重新创建连接
        logger.warn("Vert.x 5.x 不支持 reconnect 方法，请重新初始化客户端");
    }

    public void reconnect(String tenantCode, MqttProfile mqttProfile) throws Exception {
        reconnect(tenantCode);
    }

    private MqttClient getClient(String tenantCode) throws BaseException {
        MqttClient client = getClientMap().get(tenantCode);
        if (ValidateUtils.isEmpty(client)) {
            throw new BaseException(MqttErrorCode.CLIENT_IS_NULL, tenantCode);
        }
        return client;
    }

    public MqttClientOptions initMqttConnectOptions(MqttProfile mqttProfile) {
        MqttClientOptions options = new MqttClientOptions();
        
        // 基本认证配置
        options.setUsername(mqttProfile.getUserName());
        options.setPassword(mqttProfile.getPassword());
        
        // 会话配置
        options.setCleanSession(mqttProfile.isCleanSession());
        options.setAutoGeneratedClientId(false);
        
        // 心跳配置
        options.setKeepAliveInterval(mqttProfile.getKeepAliveInterval());
        options.setAutoKeepAlive(true);
        
        // 消息流控配置
        options.setMaxInflightQueue(mqttProfile.getMaxInflight());
        options.setAckTimeout(mqttProfile.getCompletionTimeout() / 1000); // 毫秒转秒
        
        // 自动确认配置
        options.setAutoAck(true);

        options.setReconnectAttempts(mqttProfile.getReconnectAttempts());
        options.setReconnectInterval(mqttProfile.getReconnectInterval());

        // 设置遗嘱消息
        MqttWill will = mqttProfile.getWill();
        if (ValidateUtils.isNotEmpty(will)) {
            options.setWillFlag(true);
            options.setWillTopic(will.getTopic());
            // Vert.x 5.x 使用 setWillMessageBytes 而不是 setWillMessage
            if (ValidateUtils.isNotEmpty(will.getMessage())) {
                options.setWillMessageBytes(Buffer.buffer(will.getMessage().getBytes(StandardCharsets.UTF_8)));
            }
            options.setWillQoS(will.getQos());
            options.setWillRetain(will.getRetained());
        }

        // SSL/TLS 配置
        if (mqttProfile.getUrl() != null && mqttProfile.getUrl().startsWith("ssl://")) {
            options.setSsl(true);
        }
        if (!mqttProfile.isVerifyCertificate()) {
            options.setTrustAll(true);
        }

        return options;
    }

    public Vertx initVertx(MqttProfile.VertxConfig vertxConfig) {
        if (vertxConfig == null) {
            return Vertx.vertx();
        }
        VertxOptions options = new VertxOptions()
                .setEventLoopPoolSize(vertxConfig.getEventLoopPoolSize())
                .setWorkerPoolSize(vertxConfig.getWorkerPoolSize())
                .setInternalBlockingPoolSize(vertxConfig.getInternalBlockingPoolSize())
                .setBlockedThreadCheckIntervalUnit(vertxConfig.getBlockedThreadCheckIntervalUnit())
                .setMaxEventLoopExecuteTimeUnit(vertxConfig.getMaxEventLoopExecuteTimeUnit())
                .setMaxWorkerExecuteTimeUnit(vertxConfig.getMaxWorkerExecuteTimeUnit())
                .setWarningExceptionTimeUnit(vertxConfig.getWarningExceptionTimeUnit())
                .setHAEnabled(vertxConfig.isHaEnabled())
                .setQuorumSize(vertxConfig.getQuorumSize())
                .setHAGroup(vertxConfig.getHaGroup())
                .setPreferNativeTransport(vertxConfig.isPreferNativeTransport())
                .setDisableTCCL(vertxConfig.isDisableTCCL());

        if (vertxConfig.getUseDaemonThread() != null) {
            options.setUseDaemonThread(vertxConfig.getUseDaemonThread());
        }

        return Vertx.vertx(options);
    }
}
