package com.steven.solomon.init;

import cn.hutool.core.lang.UUID;
import com.steven.solomon.annotation.ActiveMQ;
import com.steven.solomon.annotation.ActiveMQRetry;
import com.steven.solomon.utils.logger.LoggerUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.ExponentialBackOff;

import javax.jms.*;

@Component
@Order(2)
@DependsOn("springUtil")
public class ActiveMQInitConfig extends AbstractMessageLineRunner<ActiveMQ> {

  private final Logger logger = LoggerUtils.logger(getClass());

  private final ConnectionFactory connectionFactory;

  private ActiveMQ activeMQ;

  private final MessageConverter messageConverter;

  @Value("${spring.application.name: }")
  private String applicationName;

  public ActiveMQInitConfig(ApplicationContext applicationContext, ConnectionFactory connectionFactory, MessageConverter messageConverter) {
      this.connectionFactory = connectionFactory;
      this.messageConverter = messageConverter;
      SpringUtil.setContext(applicationContext);
  }

  @Override
  public void init(List<Object> clazzList) throws Exception {
      String clientId = ValidateUtils.getOrDefault(applicationName, UUID.randomUUID().toString());
      for(Object abstractConsumer : clazzList) {
          activeMQ = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), ActiveMQ.class);
          if(ValidateUtils.isEmpty(activeMQ)){
              logger.error("{}没有ActiveMQ注解,不进行初始化",abstractConsumer.getClass().getSimpleName());
              continue;
          }
          DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
          factory.setConnectionFactory(connectionFactory);
          factory.setMessageConverter(messageConverter);
          factory.setAutoStartup(true);
          factory.setSessionAcknowledgeMode(activeMQ.sessionAcknowledgeMode());
          factory.setSessionTransacted(activeMQ.sessionTransacted());
          factory.setClientId(clientId);
          factory.setSubscriptionDurable(activeMQ.isPersistence());
          factory.setSubscriptionShared(activeMQ.subscriptionShared());
          factory.setConcurrency(activeMQ.concurrency());
          factory.setMaxMessagesPerTask(activeMQ.maxMessagesPerTask());
          factory.setReceiveTimeout(activeMQ.receiveTimeout());
          factory.setRecoveryInterval(activeMQ.recoveryInterval());
          setBackOff(abstractConsumer,factory);
      }
  }

  private void setBackOff(Object abstractConsumer,DefaultJmsListenerContainerFactory factory){
      ActiveMQRetry activeMQRetry = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), ActiveMQRetry.class);
      if(ValidateUtils.isEmpty(activeMQRetry)){
          return;
      }
      ExponentialBackOff exponentialBackOff = new ExponentialBackOff();
      exponentialBackOff.setInitialInterval(activeMQRetry.initialInterval());  // 初始重试间隔1秒
      exponentialBackOff.setMaxInterval(activeMQRetry.maxInterval());    // 最大重试间隔10秒
      exponentialBackOff.setMultiplier(activeMQRetry.multiplier());        // 每次重试间隔乘数因子为2
      exponentialBackOff.setMaxElapsedTime(activeMQRetry.maxInterval()); // 最大运行时间30秒
      factory.setBackOff(exponentialBackOff);
  }
}
