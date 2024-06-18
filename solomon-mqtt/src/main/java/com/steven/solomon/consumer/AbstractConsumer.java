package com.steven.solomon.consumer;

import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;

public abstract class AbstractConsumer<T,R> implements IMqttMessageListener {

  protected final Logger logger = LoggerUtils.logger(getClass());

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    String json          = new String(message.getPayload(), StandardCharsets.UTF_8);
    try {
      if(checkMessageKey(topic,message)){
        throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
      }
      MqttModel mqttModel = JackJsonUtils.conversionClass(json, MqttModel.class);
      RequestHeaderHolder.setTenantCode(mqttModel.getTenantCode());
      logger.info("线程名:{},租户编码为:{},topic主题:{},AbstractConsumer:消费者消息: {}",Thread.currentThread().getName(),mqttModel.getTenantCode(),topic, json);
      // 消费者消费消息
      R result = this.handleMessage(topic,mqttModel.getTenantCode(),(T) mqttModel.getBody());
      //保存消费成功消息
      saveLog(result,message,mqttModel);
    } catch (Throwable e){
      logger.error("AbstractConsumer:消费报错,消息为:{}, 异常为:",json, e);
      saveFailMessage(topic,message,e);
    } finally {
      deleteCheckMessageKey(topic,message);
    }
  }

  /**
   * 消费方法
   * @param body 请求数据
   */
  public abstract R handleMessage(String topic,String tenantCode, T body) throws Exception;

  /**
   * 保存消费失败的消息
   *
   * @param message mq所包含的信息
   * @param e 异常
   */
  public void saveFailMessage(String topic,MqttMessage message, Throwable e){

  }

  /**
   * 判断是否重复消费
   * @return true 重复消费 false 不重复消费
   */
  public boolean checkMessageKey(String topic, MqttMessage message){
    return false;
  }

  /**
   * 保存消费成功消息
   * @param result 消费成功后返回的结果
   * @param message mqtt消息题
   * @param model 收到的消息体
   */
  public abstract void saveLog(R result,MqttMessage message,MqttModel model);

  /**
   * 删除判断重复消费Key
   */
  public void deleteCheckMessageKey(String topic, MqttMessage message){

  }
}
