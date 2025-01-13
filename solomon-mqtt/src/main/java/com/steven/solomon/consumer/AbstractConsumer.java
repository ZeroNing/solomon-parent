package com.steven.solomon.consumer;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.mq.CommonMqttMessageListener;
import com.steven.solomon.utils.logger.LoggerUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import com.steven.solomon.verification.ValidateUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;

public abstract class AbstractConsumer<T,R> implements IMqttMessageListener, CommonMqttMessageListener<T,R,MqttModel<T>> {

  protected final Logger logger = LoggerUtils.logger(getClass());

  protected String topic;

  protected MqttMessage mqttMessage;

  protected String tenantCode;

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    this.topic = topic;
    this.mqttMessage = message;

    String json          = new String(message.getPayload(), StandardCharsets.UTF_8);
    Throwable throwable = null;
    R result = null;
    MqttModel<T> model = null;
    try {
      model = conversion(json);
      tenantCode = model.getTenantCode();
      logger.info("线程名:{},租户编码为:{},消息ID:{},topic主题:{},AbstractConsumer:消费者消息: {}",Thread.currentThread().getName(),tenantCode,message.getId(),topic, json);
      // 判断是否重复消费
      if(checkMessageKey(model)){
        throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
      }
      if(ValidateUtils.isNotEmpty(tenantCode)){
        RequestHeaderHolder.setTenantCode(tenantCode);
      }
      // 消费消息
      result = this.handleMessage(model.getBody());
    } catch (Throwable e){
      logger.error("AbstractConsumer:消费报错,消息为:{}, 异常为:",json, e);
      throwable = e;
    } finally {
      deleteCheckMessageKey(model);
      // 保存消费成功/失败消息
      saveLog(result,throwable,model);
    }
  }
}
