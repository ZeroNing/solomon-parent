package com.steven.solomon.service;

import com.steven.solomon.annotation.RabbitMq;
import com.steven.solomon.entity.InitRabbitBinding;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public abstract class AbstractMQService  {

  public static String SERVICE_NAME = "MQService";

  public Queue initBinding(RabbitMq rabbitMq,String queueName, RabbitAdmin admin, boolean isInitDlxMap, boolean isAddDlxPrefix) {
    InitRabbitBinding initRabbitBinding = new InitRabbitBinding(rabbitMq,queueName, isInitDlxMap, isAddDlxPrefix);
    // 初始化队列
    Queue queue = new Queue(initRabbitBinding.getQueue(), rabbitMq.isPersistence(), false, false,initRabbitBinding.getArgs());
    // 绑定队列
    admin.declareQueue(queue);
    // 绑定交换机
    AbstractExchange exchange = initExchange(initRabbitBinding.getExchange(),rabbitMq);
    admin.declareExchange(exchange);
    // 绑定
    admin.declareBinding(this.initBinding(queue,exchange,initRabbitBinding.getRoutingKey(),rabbitMq));
    return queue;
  }

  /**
   * 初始化交换机
   */
  protected abstract AbstractExchange initExchange(String exchange,RabbitMq rabbitMq);

  /**
   * 初始化绑定
   * @return
   */
  protected abstract Binding initBinding(Queue queue,AbstractExchange exchange,String routingKey,RabbitMq rabbitMq);
}
