package com.steven.solomon.consumer;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.mq.CommonMqttMessageListener;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.dromara.mica.mqtt.codec.message.MqttPublishMessage;
import org.dromara.mica.mqtt.core.client.IMqttClientMessageListener;
import org.slf4j.Logger;
import org.tio.core.ChannelContext;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 消费者抽象类
 */
public abstract class AbstractConsumer<T, R> implements IMqttClientMessageListener, CommonMqttMessageListener<T,R, MqttModel<T>> {

    protected final Logger logger = LoggerUtils.logger(getClass());

    protected String topic;

    protected String tenantCode;

    @Override
    public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload){
        this.topic = topic;

        String json          = new String(payload, StandardCharsets.UTF_8);
        Throwable throwable = null;
        R result = null;
        MqttModel<T> model = null;
        try {
            model = conversion(json);
            tenantCode = model.getTenantCode();
            logger.info("线程名:{},租户编码为:{},topic主题:{},{}:消费者消息: {}",Thread.currentThread().getName(),tenantCode,topic,getClass().getName(), json);
            // 判断是否重复消费
            if (checkMessageKey(model)) {
                throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
            }
            if (ValidateUtils.isNotEmpty(tenantCode)) {
                RequestHeaderHolder.setTenantCode(tenantCode);
            }
            // 消费消息
            result = this.handleMessage(model.getBody());
        } catch (Throwable e) {
            logger.error("{}:消费报错,消息为:{}, 异常为:",getClass().getName(),json, e);
            throwable = e;
        } finally {
            deleteCheckMessageKey(model);
            // 保存消费成功/失败消息
            saveLog(result,throwable,model);
        }
    }

}
