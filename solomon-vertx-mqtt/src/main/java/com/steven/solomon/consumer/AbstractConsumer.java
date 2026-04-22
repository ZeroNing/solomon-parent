package com.steven.solomon.consumer;

import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.mq.CommonMqttMessageListener;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import io.vertx.mqtt.messages.MqttPublishMessage;
import org.slf4j.Logger;

public abstract class AbstractConsumer<T, R> implements CommonMqttMessageListener<T, R, MqttModel<T>> {

    protected final Logger logger = LoggerUtils.logger(getClass());

    protected String tenantCode;

    public void messageArrived(String topic, MqttPublishMessage message) throws Exception {
        String json = message.payload().toString(StandardCharsets.UTF_8);
        Throwable throwable = null;
        R result = null;
        MqttModel<T> model = null;
        try {
            logger.info("线程名:{},租户编码为:{},消息ID:{},topic主题:{},AbstractConsumer:消费者消息: {}", Thread.currentThread().getName(), tenantCode, message.messageId(), topic, json);
            model = conversion(json);
            tenantCode = model.getTenantCode();
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
            logger.error("AbstractConsumer:消费报错,消息为:{}, 异常为:", json, e);
            throwable = e;
        } finally {
            deleteCheckMessageKey(model);
            // 保存消费成功/失败消息
            saveLog(result, throwable, model);
        }
    }
}
