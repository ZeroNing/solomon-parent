package com.steven.solomon.utils;

import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.init.RabbitMQInitConfig;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.pojo.BaseMq;
import com.steven.solomon.pojo.MessageQueueDatail;
import com.steven.solomon.pojo.RabbitMqModel;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class RabbitUtils {

  private Logger logger = LoggerUtils.logger(RabbitUtils.class);

  @Resource
  private RabbitTemplate rabbitTemplate;

  /**
   * 发送消息
   */
  public void send(BaseMq mq) throws Exception {
    if (!convertAndSend(mq,0,false)) {
      throw new BaseException(BaseExceptionCode.BASE_EXCEPTION_CODE);
    }
  }

  /**
   * 发送延缓信息
   */
  public void sendDelay(BaseMq mq, long delay) throws Exception {
    if (!convertAndSend(mq,delay,true)) {
      throw new BaseException(BaseExceptionCode.BASE_EXCEPTION_CODE);
    }
  }

  /**
   * 发送消息,并设置消息过期时间
   * @param mq
   * @param expiration
   * @throws Exception
   */
  public void sendExpiration(BaseMq mq, long expiration) throws Exception {
    if (!convertAndSend(mq,expiration,false)) {
      throw new BaseException(BaseExceptionCode.BASE_EXCEPTION_CODE);
    }
  }

  /**
   * 重置队列并发使用者
   */
  public boolean resetQueueConcurrentConsumers(String queueName, int concurrentConsumers) {
    Assert.state(concurrentConsumers > 0, "参数 'concurrentConsumers' 必须大于0.");
    DirectMessageListenerContainer container = (DirectMessageListenerContainer)findContainerByQueueName(queueName);
    if (ValidateUtils.isNotEmpty(container) && container.isActive() && container.isRunning()) {
      container.setConsumersPerQueue(concurrentConsumers);
      return true;
    }
    return false;
  }

  /**
   * 重启消息监听者
   */
  public boolean restartMessageListener(String queueName) {
    if(ValidateUtils.isEmpty(queueName)){
      logger.info("restartMessageListener 重启队列失败,传入队列名为空!");
      return false;
    }
    DirectMessageListenerContainer container = (DirectMessageListenerContainer)findContainerByQueueName(queueName);
    if(ValidateUtils.isEmpty(container)){
      logger.info("restartMessageListener 停止队列失败,没有这个监听器");
      return false;
    }
    Assert.state(!container.isRunning(), String.format("消息队列%s对应的监听容器正在运行！", queueName));
    container.start();
    return true;
  }
  /**
   * 停止消息监听者
   */
  public boolean stopMessageListener(String queueName) {
    if(ValidateUtils.isEmpty(queueName)){
      logger.info("stopMessageListener 停止队列失败,传入队列名为空!");
      return false;
    }
    DirectMessageListenerContainer container = (DirectMessageListenerContainer)findContainerByQueueName(queueName);
    if(ValidateUtils.isEmpty(container)){
      logger.info("stopMessageListener 停止队列失败,没有这个监听器");
      return false;
    }
    Assert.state(container.isRunning(), String.format("消息队列%s对应的监听容器未运行！", queueName));
    container.stop();
    return true;
  }

  //  private Map<String, AbstractMessageListenerContainer> getQueue2ContainerAllMap() {
//    if (!hasInit) {
//      synchronized (allQueueContainerMap) {
//        if (!hasInit) {
//          Collection<MessageListenerContainer> listenerContainers = SpringUtil.getBean(RabbitListenerEndpointRegistry.class).getListenerContainers();
//          listenerContainers.forEach(container -> {
//                    	DirectMessageListenerContainer simpleContainer = (DirectMessageListenerContainer) container;
//                        Arrays.stream(simpleContainer.getQueueNames()).forEach(queueName ->
//                        allQueueContainerMap.putIfAbsent(StringUtils.trim(queueName), simpleContainer));
//                    });
//          hasInit = true;
//        }
//      }
//    }
//    return allQueueContainerMap;
//  }

  private boolean convertAndSend(BaseMq baseMq, long expiration,boolean isDelayed) {
    RabbitMqModel rabbitMQModel = (RabbitMqModel) baseMq;
    if (ValidateUtils.isEmpty(rabbitMQModel) || ValidateUtils.isEmpty(rabbitMQModel.getExchange())) {
      return false;
    }

    rabbitTemplate.convertAndSend(rabbitMQModel.getExchange(), rabbitMQModel.getRoutingKey(), rabbitMQModel,msg->{
      if(ValidateUtils.equals(0,expiration)){
        return msg;
      }
      if(isDelayed){
        msg.getMessageProperties().setHeader("x-delay",expiration);
      } else {
        msg.getMessageProperties().setExpiration(String.valueOf(expiration));
      }
      return msg;
    },new CorrelationData(
        UUID.randomUUID().toString()));
    return true;
  }

  public Collection<AbstractMessageListenerContainer> getAllQueueContainerList(){
    return RabbitMQInitConfig.allQueueContainerMap.values();
  }

  public List<MessageQueueDatail> statAllMessageQueueDetail() {
    List<MessageQueueDatail> queueDetailList = new ArrayList<>();
    RabbitMQInitConfig.allQueueContainerMap.entrySet().forEach(entry -> {
      String                           queueName = entry.getKey();
      AbstractMessageListenerContainer container = entry.getValue();
      MessageQueueDatail               deatil    = new MessageQueueDatail(queueName, container);
      queueDetailList.add(deatil);
    });

    return queueDetailList;
  }

  public AbstractMessageListenerContainer findContainerByQueueName(String queueName) {
    String                         key       = StringUtils.trim(queueName);
    AbstractMessageListenerContainer container = RabbitMQInitConfig.allQueueContainerMap.get(key);
    return container;
  }
}