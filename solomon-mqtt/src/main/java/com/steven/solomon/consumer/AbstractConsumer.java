package com.steven.solomon.consumer;

import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.logger.LoggerUtils;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;

public abstract class AbstractConsumer<T> implements IMqttMessageListener {

  protected final Logger logger = LoggerUtils.logger(getClass());

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    try {
      if(checkMessageKey(topic,message)){
        throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
      }
      String        json          = new String(message.getPayload(), StandardCharsets.UTF_8);
      logger.info("线程名:{},AbstractConsumer:消费者消息: {}",Thread.currentThread().getName(), json);

      RabbitMqModel rabbitMqModel = JackJsonUtils.conversionClass(json, RabbitMqModel.class);
      // 消费者消费消息
      this.handleMessage((T) rabbitMqModel.getBody());
    } catch (Exception e){
      logger.info("AbstractConsumer:消费报错 异常为:", e);
      saveFailMessage(topic,message,e);
    }
  }

  /**
   * 消费方法
   * @param body 请求数据
   */
  public abstract void handleMessage(T body) throws Exception;

  /**
   * 保存消费失败的消息
   *
   * @param message mq所包含的信息
   * @param e 异常
   */
  public abstract void saveFailMessage(String topic,MqttMessage message, Exception e);

  /**
   * 判断是否重复消费
   * @return true 重复消费 false 不重复消费
   */
  public boolean checkMessageKey(String topic, MqttMessage message){
    return false;
  }
}
