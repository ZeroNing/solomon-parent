package com.steven.solomon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.BackOff;

@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ActiveMQ {

  @AliasFor(annotation = Component.class)
  String value() default "";

  /**
   * 队列名或者主题名
   */
  String name();

  /**
   * 是否是队列模式 默认队列模式 false则是主题模式
   */
  boolean isQueue() default true;

  /**
   * 是否开启事务
   */
  boolean sessionTransacted() default true;

  /**
   * 是否持久化
   */
  boolean isPersistence() default true;

  /**
   * 设置确认模式
   * AUTO_ACKNOWLEDGE = 1 ：自动确认
   * CLIENT_ACKNOWLEDGE = 2：客户端手动确认
   * DUPS_OK_ACKNOWLEDGE = 3： 自动批量确认
   * SESSION_TRANSACTED = 0：事务提交并确认
   * 但是在activemq补充了一个自定义的ACK模式:
   * INDIVIDUAL_ACKNOWLEDGE = 4：单条消息确认
   */
  int sessionAcknowledgeMode() default 2;

  /**
   * 默认使用queue模式，使用topic则需要设置为true
   */
  boolean pubSubDomain() default false;

  /**
   * 指定订阅是否为共享订阅。共享订阅允许多个消费者共享同一个订阅，负载均衡地接收消息
   */
  boolean subscriptionShared() default false;

  /**
   * 它定义了监听器容器的并发级别，即同时处理消息的消费者线程数量
   */
  String concurrency() default "1";

  /**
   * 设置每个任务处理的最大消息数。可以限制每个线程在一次执行中处理的消息数，防止某个线程长期占用资源
   */
  int maxMessagesPerTask() default 10;

  /**
   * 设置接收消息的超时时间，单位为毫秒 默认:30秒
   */
  long receiveTimeout() default 30000L;

  /**
   * 设置监听器在尝试恢复连接之前的等待时间，单位为毫秒 默认: 5秒
   */
  long recoveryInterval() default 5000L;

}
