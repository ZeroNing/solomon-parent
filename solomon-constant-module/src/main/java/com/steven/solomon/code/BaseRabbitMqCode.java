package com.steven.solomon.code;

/**
 * RabbitMQ 基础常量接口，定义消息队列通用编码。
 */
public interface BaseRabbitMqCode {

  /** 死信交换机/队列名称前缀。 */
  String DLX_PREFIX = "dlx_";
}
