package com.steven.solomon.rabbitmq.code;

import com.steven.solomon.code.BaseExceptionCode;

public interface RabbitMqErrorCode extends BaseExceptionCode {

  String REPLY_TO_IS_NULL = "REPLY_TO_IS_NULL";

  String ENABLED = "ENABLED";
}
