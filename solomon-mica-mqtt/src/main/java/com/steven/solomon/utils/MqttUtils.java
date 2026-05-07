package com.steven.solomon.utils;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.code.MqttErrorCode;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.service.SendService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.codec.message.builder.MqttTopicSubscription;
import org.dromara.mica.mqtt.core.client.IMqttClientConnectListener;
import org.dromara.mica.mqtt.core.client.MqttClient;
import org.dromara.mica.mqtt.core.client.MqttClientCreator;
import org.dromara.mica.mqtt.core.client.MqttWillMessage;
import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.tio.core.ChannelContext;
import org.tio.core.ssl.SslConfig;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class MqttUtils implements SendService<MqttModel<?>> {

    private final Logger logger = LoggerUtils.logger(MqttUtils.class);

    private final Map<String, MqttClient> clientMap = new HashMap<>();

    private final Map<String, MqttClientProperties> profileMap = new HashMap<>();

    public Map<String, MqttClient> getClientMap() {
        return clientMap;
    }

    public void putClient(String tenantCode, MqttClient client) {
        this.clientMap.put(tenantCode, client);
    }

    public Map<String, MqttClientProperties> getProfileMap() {
        return profileMap;
    }

    public void putProfile(String tenantCode, MqttClientProperties profile) {
        this.profileMap.put(tenantCode, profile);
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
            String json = JSONUtil.toJsonStr(data.getBody());
            client.publish(data.getTopic(), json.getBytes(StandardCharsets.UTF_8), 
                    MqttQoS.valueOf(data.getQos()), data.getRetained());
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
        // AbstractConsumer 已经实现了 IMqttClientMessageListener，可以直接传入
        client.subscribe(topic, MqttQoS.valueOf(qos), consumer);
    }

    /**
     * 订阅消息
     *
     * @param client     mqtt连接
     */
    public void subscribe(MqttClient client, String tenantCode) {
        List<Object> clazzList = new ArrayList<>(SpringUtil.getBeansWithAnnotation(MessageListener.class).values());
        this.subscribe(client, clazzList, tenantCode);
    }

    /**
     * 订阅消息
     *
     * @param client     mqtt连接
     */
    public void subscribe(MqttClient client, List<Object> clazzList, String tenantCode) {
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
                        // AbstractConsumer 已经实现了 IMqttClientMessageListener，可以直接使用
                        AbstractConsumer<?, ?> consumer = (AbstractConsumer<?, ?>) BeanUtil.copyProperties(abstractConsumer, abstractConsumer.getClass(), (String) null);
                        int qos = messageListener.qos();
                        client.subscribe(topic, MqttQoS.valueOf(qos), consumer);
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
    public void unsubscribe(String tenantCode, String[] topic) throws BaseException {
        if (ValidateUtils.isEmpty(topic)) {
            return;
        }
        MqttClient client = getClient(tenantCode);
        for (String t : topic) {
            client.unSubscribe(t);
        }
    }

    /**
     * 关闭连接
     */
    public void disconnect(String tenantCode) throws BaseException {
        MqttClient client = getClient(tenantCode);
        if (client != null) {
            client.disconnect();
        }
    }

    /**
     * 重新连接
     */
    public void reconnect(String tenantCode) throws BaseException {
        MqttClient client = getClient(tenantCode);
        if (client != null && !client.isConnected()) {
            MqttClientProperties profile = getProfileMap().get(tenantCode);
            if (profile != null) {
                client.reconnect();
                subscribe(client, tenantCode);
            }
        }
    }

    public void reconnect(String tenantCode, MqttClientProperties mqttProfile) throws BaseException {
        MqttClient client = getClient(tenantCode);
        if (client != null && !client.isConnected()) {
            client.reconnect();
            subscribe(client, tenantCode);
        }
    }

    private MqttClient getClient(String tenantCode) throws BaseException {
        MqttClient client = getClientMap().get(tenantCode);
        if (ValidateUtils.isEmpty(client)) {
            throw new BaseException(MqttErrorCode.MQTT_CLIENT_IS_NULL, tenantCode);
        }
        return client;
    }

    /**
     * 创建 Mica MQTT 客户端
     *
     * @param tenantCode  租户编码
     * @param properties MQTT配置
     * @return MqttClient
     */
    public MqttClient createMqttClient(String tenantCode, MqttClientProperties properties) {
        putProfile(tenantCode, properties);
        // 将 MqttClientProperties 中的属性逐一映射到 Creator
        MqttClientCreator clientCreator = MqttClient.create()
                .name(properties.getName())
                .ip(properties.getIp())
                .port(properties.getPort())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .clientId(properties.getClientId())
                .bindIp(properties.getBindIp())
                .bindNetworkInterface(properties.getBindNetworkInterface())
                .readBufferSize((int) properties.getReadBufferSize().toBytes())
                .maxBytesInMessage((int) properties.getMaxBytesInMessage().toBytes())
                .maxClientIdLength(properties.getMaxClientIdLength())
                .keepAliveSecs(properties.getKeepAliveSecs())
                .heartbeatMode(properties.getHeartbeatMode())
                .heartbeatTimeoutStrategy(properties.getHeartbeatTimeoutStrategy())
                .reconnect(properties.isReconnect())
                .reInterval(properties.getReInterval())
                .retryCount(properties.getRetryCount())
                .reSubscribeBatchSize(properties.getReSubscribeBatchSize())
                .version(properties.getVersion())
                .cleanStart(properties.isCleanStart())
                .sessionExpiryIntervalSecs(properties.getSessionExpiryIntervalSecs())
                .statEnable(properties.isStatEnable())
                .debug(properties.isDebug())
                .disconnectBeforeStop(properties.isDisconnectBeforeStop());
        Integer timeout = properties.getTimeout();
        if (timeout != null && timeout > 0) {
            clientCreator.timeout(timeout);
        }
        // mqtt 业务线程数
        Integer bizThreadPoolSize = properties.getBizThreadPoolSize();
        if (bizThreadPoolSize != null && bizThreadPoolSize > 0) {
            clientCreator.bizThreadPoolSize(bizThreadPoolSize);
        }
        // 开启 ssl
//        MqttClientProperties.Ssl ssl = properties.getSsl();
//        if (ssl.isEnabled()) {
//            SslConfig sslConfig = SslConfig.forClient(ssl.getKeystorePath(), ssl.getKeystorePass(), ssl.getTruststorePath(), ssl.getTruststorePass());
//            clientCreator.sslConfig(sslConfig);
//            sslCustomizers.ifAvailable(sslConfig::setSslEngineCustomizer);
//        }
        // 构造遗嘱消息
        MqttClientProperties.WillMessage willMessage = properties.getWillMessage();
        if (willMessage != null && StringUtils.hasText(willMessage.getTopic())) {
            clientCreator.willMessage(builder -> {
                builder.topic(willMessage.getTopic())
                        .qos(willMessage.getQos())
                        .retain(willMessage.isRetain());
                if (StringUtils.hasText(willMessage.getMessage())) {
                    builder.messageText(willMessage.getMessage());
                }
            });
        }
        // 全局订阅
        List<MqttTopicSubscription> globalSubscribe = properties.getGlobalSubscribe();
        if (globalSubscribe != null && !globalSubscribe.isEmpty()) {
            clientCreator.globalSubscribe(globalSubscribe);
        }
        MqttClient client = clientCreator.connect();
        putClient(tenantCode, client);
        return client;
    }

}
