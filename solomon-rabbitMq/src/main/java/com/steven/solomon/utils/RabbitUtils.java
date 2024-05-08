package com.steven.solomon.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.steven.solomon.annotation.RabbitMq;
import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MessageQueueDatail;
import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.init.RabbitMQInitConfig;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.pojo.entity.BaseMq;
import com.steven.solomon.service.SendService;
import com.steven.solomon.verification.ValidateUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Configuration
public class RabbitUtils implements SendService<RabbitMqModel> {

  private Logger logger = LoggerUtils.logger(RabbitUtils.class);

  private final RabbitTemplate rabbitTemplate;

  public RabbitUtils(RabbitTemplate rabbitTemplate) {this.rabbitTemplate = rabbitTemplate;}

  /**
   * 发送消息
   */
  @Override
  public void send(RabbitMqModel mq) throws Exception {
    if (!convertAndSend(mq,0,false)) {
      throw new BaseException(BaseExceptionCode.BASE_EXCEPTION_CODE);
    }
  }

  /**
   * 发送延缓信息
   */
  @Override
  public void sendDelay(RabbitMqModel mq, long delay) throws Exception {
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
  @Override
  public void sendExpiration(RabbitMqModel mq, long expiration) throws Exception {
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
      logger.debug("restartMessageListener 重启队列失败,传入队列名为空!");
      return false;
    }
    DirectMessageListenerContainer container = (DirectMessageListenerContainer)findContainerByQueueName(queueName);
    if(ValidateUtils.isEmpty(container)){
      logger.debug("restartMessageListener 停止队列失败,没有这个监听器");
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
      logger.debug("stopMessageListener 停止队列失败,传入队列名为空!");
      return false;
    }
    DirectMessageListenerContainer container = (DirectMessageListenerContainer)findContainerByQueueName(queueName);
    if(ValidateUtils.isEmpty(container)){
      logger.debug("stopMessageListener 停止队列失败,没有这个监听器");
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

  private boolean convertAndSend(BaseMq baseMq, long expiration, boolean isDelayed) {
    RabbitMqModel rabbitMQModel = (RabbitMqModel) baseMq;
    if (ValidateUtils.isEmpty(rabbitMQModel) || ValidateUtils.isEmpty(rabbitMQModel.getExchange())) {
      return false;
    }
    Map<String,Object> headers = rabbitMQModel.getHeaders();
    rabbitTemplate.convertAndSend(rabbitMQModel.getExchange(), rabbitMQModel.getRoutingKey(), rabbitMQModel,msg->{
      if(ValidateUtils.equals(0L,expiration)){
        return msg;
      }
      if(ValidateUtils.isNotEmpty(headers)){
        for(Entry<String,Object> entry : headers.entrySet()){
          msg.getMessageProperties().setHeader(entry.getKey(),entry.getValue());
        }
      }
      if(isDelayed){
        msg.getMessageProperties().setHeader("x-delay",expiration);
      } else {
        msg.getMessageProperties().setExpiration(String.valueOf(expiration));
      }
      return msg;
    },new CorrelationData(UUID.randomUUID().toString()));
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

  /**
   * 手动拉去消息消费,根据队列名找到对应消费器消费
   * @param transactional 是否开启事务
   * @param queueName     队列名
   * @throws Exception
   */
  public void handleQueueMessageManually(boolean transactional,String queueName) throws Exception {
    Channel     channel  = rabbitTemplate.getConnectionFactory().createConnection().createChannel(transactional);
    GetResponse response = channel.basicGet(queueName, false);
    if (ValidateUtils.isEmpty(response)) {
      logger.debug("没有从{}队列中获取到消息",queueName);
      return;
    }
    Map<String,Object> annotationMap = SpringUtil.getBeansWithAnnotation(RabbitMq.class);
    for(Object obj : annotationMap.values()){
      RabbitMq     rabbitMq = AnnotationUtils.findAnnotation(obj.getClass(), RabbitMq.class);
      List<String> queues   = Arrays.asList(rabbitMq.queues());
      if(!queues.contains(queueName)){
        continue;
      }
      logger.debug("从{}队列找到消费者类:{}",queueName,obj.getClass().getSimpleName());
      AbstractConsumer abstractConsumer = (AbstractConsumer) obj;
      abstractConsumer.onMessage(new Message(response.getBody(), new MessageProperties()),channel);
    }
  }
}