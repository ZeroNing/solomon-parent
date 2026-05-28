package com.steven.solomon.code;

/**
 * 消息队列异常错误码接口，继承基础异常编码并扩展MQ特有的错误码。
 *
 * <p>定义消息消费相关的异常场景，如无对应实现和消息重复消费。</p>
 */
public interface MqErrorCode extends BaseExceptionCode {

  /** 无对应的消息消费者实现。 */
  String NO_CORRESPONDING_IMPLEMENTATION="NO_CORRESPONDING_IMPLEMENTATION";

  /** 消息重复消费（幂等拦截）。 */
  String MESSAGE_REPEAT_CONSUMPTION = "MESSAGE_REPEAT_CONSUMPTION";
}
