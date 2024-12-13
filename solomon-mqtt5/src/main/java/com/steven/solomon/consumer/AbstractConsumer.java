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
import com.steven.solomon.utils.logger.LoggerUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;

public abstract class AbstractConsumer<T,R> implements IMqttMessageListener {

  protected final Logger logger = LoggerUtils.logger(getClass());

  protected String topic;

  protected MqttMessage mqttMessage;

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    this.topic = topic;
    this.mqttMessage = message;

    String json          = new String(message.getPayload(), StandardCharsets.UTF_8);
    Throwable throwable = null;
    R result = null;
    MqttModel<T> mqttModel = null;
    try {
      mqttModel = conversion(json);
      RequestHeaderHolder.setTenantCode(mqttModel.getTenantCode());
      logger.info("线程名:{},租户编码为:{},消息ID:{},topic主题:{},AbstractConsumer:消费者消息: {}",Thread.currentThread().getName(),mqttModel.getTenantCode(),message.getId(),topic, json);
      // 判断是否重复消费
      if(checkMessageKey(mqttModel)){
        throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
      }
      // 消费消息
      result = this.handleMessage(topic,mqttModel.getTenantCode(),mqttModel.getBody());
    } catch (Throwable e){
      logger.error("AbstractConsumer:消费报错,消息为:{}, 异常为:",json, e);
      throwable = e;
    } finally {
      deleteCheckMessageKey(topic,message);
      // 保存消费成功/失败消息
      saveLog(result,mqttModel,throwable);
    }
  }

  /**
   * 消费方法
   * @param body 请求数据
   */
  public abstract R handleMessage(String topic,String tenantCode, T body) throws Exception;

  /**
   * 判断是否重复消费
   * @return true 重复消费 false 不重复消费
   */
  public boolean checkMessageKey(MqttModel<T> mqttModel){
    return false;
  }

  /**
   * 保存消费成功/失败的消息
   * @param result 消费成功后返回的结果
   * @param model 收到的消息体
   */
  public abstract void saveLog(R result,MqttModel<T> model, Throwable e);

  /**
   * 删除判断重复消费Key
   */
  public void deleteCheckMessageKey(String topic, MqttMessage message){

  }

  private MqttModel<T> conversion(String json) {
    MqttModel<T> model = JSONUtil.toBean(json, new TypeReference<MqttModel<T>>() {},true);
    T body = model.getBody();
    boolean isJsonObject = body instanceof JSONObject;
    boolean isJsonArray = body instanceof JSONArray;
    if(!isJsonObject && !isJsonArray){
      return model;
    }
    Type typeArgument = TypeUtil.getTypeArgument(getClass(),0);
    body = JSONUtil.toBean(JSONUtil.toJsonStr(body),typeArgument,true);
    model.setBody(body);
    return model;
  }
}
