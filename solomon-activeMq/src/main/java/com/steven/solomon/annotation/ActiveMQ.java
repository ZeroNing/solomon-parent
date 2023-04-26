package com.steven.solomon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

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
}
