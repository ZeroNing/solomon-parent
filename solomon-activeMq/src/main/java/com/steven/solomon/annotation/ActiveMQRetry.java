package com.steven.solomon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ActiveMQRetry {

  /**
   * 是否在每次尝试重新发送失败后,增长这个等待时间(默认是false 不增长)
   */
  boolean useExponentialBackOff() default false;

  /**
   * 重试次数
   */
  int retryNumber() default 2;

  /**
   * 重发时间间隔,默认为1000ms（1秒）
   */
  long redeliveryDelay() default 1000L;

  /**
   * 重发时长递增的时间倍数
   */
  int backOffMultiplier() default 2;

  /**
   * 是否避免消息碰撞 默认为true
   */
  boolean useCollisionAvoidance() default true;

  /**
   * 设置重发最大拖延时间-1表示无延迟限制
   */
  long maximumRedeliveryDelay() default -1;

  /**
   * 首次重试时间间隔，默认为0毫秒
   */
  long initialRedeliveryDelay()default 0;
}
