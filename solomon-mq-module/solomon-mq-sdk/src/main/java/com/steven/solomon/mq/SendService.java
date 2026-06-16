package com.steven.solomon.mq;

import com.steven.solomon.mq.model.BaseMq;

/**
 * 消息发送服务接口，定义消息队列发送的基本操作。
 *
 * <p>支持即时发送、延时发送和过期发送三种模式，由各 MQ 实现模块
 * （RabbitMQ、RocketMQ、Kafka、MQTT 等）提供具体适配。</p>
 *
 * @param <T> 消息实体类型，必须继承 {@link BaseMq}
 * @author steven
 */
public interface SendService<T extends BaseMq> {

  /**
   * 即时发送消息。
   *
   * @param data 消息实体
   * @throws Exception 发送过程中可能抛出的异常
   */
  void send(T data) throws Exception;

  /**
   * 发送延时消息，经过指定延迟时间后投递到消费端。
   *
   * @param data  消息实体
   * @param delay 延迟时间（毫秒）
   * @throws Exception 发送过程中可能抛出的异常
   */
  void sendDelay(T data, long delay) throws Exception;

  /**
   * 发送消息并设置过期时间，超过过期时间后消息将被丢弃。
   *
   * @param data       消息实体
   * @param expiration 过期时间（毫秒）
   * @throws Exception 发送过程中可能抛出的异常
   */
  void sendExpiration(T data, long expiration) throws Exception;
}
