package com.steven.solomon.init;

import com.steven.solomon.annotation.ActiveMQ;
import com.steven.solomon.annotation.ActiveMQRetry;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.spring.SpringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.steven.solomon.verification.ValidateUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.region.policy.RedeliveryPolicyMap;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.collections4.map.SingletonMap;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
@Order(2)
@DependsOn("springUtil")
public class ActiveMQInitConfig implements CommandLineRunner {

  private final Logger logger = LoggerUtils.logger(getClass());


  private final ActiveMQConnectionFactory connectionFactory;

  private ActiveMQ activeMQ;

  private ActiveMQRetry activeMQRetry;

  public ActiveMQInitConfig(ActiveMQConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void run(String... args) throws Exception {
    this.init(new ArrayList<>(SpringUtil.getBeansWithAnnotation(ActiveMQ.class).values()));
  }

  public void init(List<Object> clazzList) throws JMSException {
    // 判断消费者队列是否存在
    if (ValidateUtils.isEmpty(clazzList)) {
      logger.info("MessageListenerConfig:没有ActiveMQ消费者");
      return;
    }
    Connection connection = connectionFactory.createConnection();
    connection.start();
    MessageConsumer consumer = null;
    RedeliveryPolicyMap redeliveryPolicyMap = new RedeliveryPolicyMap();

    for(Object abstractConsumer : clazzList){
      activeMQ = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), ActiveMQ.class);
      boolean isQueue = activeMQ.isQueue();
      Queue queue = null;
      Topic topic = null;
      Session session = connection.createSession(false,activeMQ.sessionAcknowledgeMode());
      String queueName = activeMQ.name();
      if(activeMQ.isPersistence()){
        queueName = queueName + "?consumer.persistent=true";
      }
      if(isQueue){
        queue = session.createQueue(queueName);
        consumer = session.createConsumer(queue);
      } else {
        topic = session.createTopic(queueName);
        consumer = session.createConsumer(topic);
      }
      consumer.setMessageListener((MessageListener) abstractConsumer);
      activeMQRetry =  AnnotationUtils.findAnnotation(abstractConsumer.getClass(), ActiveMQRetry.class);
      if(ValidateUtils.isEmpty(activeMQRetry)){
        continue;
      }

      RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
      redeliveryPolicy.setMaximumRedeliveries(activeMQRetry.retryNumber());
      redeliveryPolicy.setRedeliveryDelay(activeMQRetry.redeliveryDelay());
      redeliveryPolicy.setUseExponentialBackOff(activeMQRetry.useExponentialBackOff());
      redeliveryPolicy.setMaximumRedeliveryDelay(activeMQRetry.maximumRedeliveryDelay());
      redeliveryPolicy.setInitialRedeliveryDelay(activeMQRetry.initialRedeliveryDelay());
      redeliveryPolicyMap.put(isQueue ? new ActiveMQQueue(activeMQ.name()) : new ActiveMQTopic(activeMQ.name()),redeliveryPolicy);
    }
    connectionFactory.setRedeliveryPolicyMap(redeliveryPolicyMap);

  }
}
