package com.steven.solomon.code;

public interface BaseRabbitMqCode {

  String DLX_ROUTING_KEY = "x-dead-letter-routing-key";

  String DLX_EXCHANGE_KEY = "x-dead-letter-exchange";

  String DLX_TTL = "x-message-ttl";

  String DLX_PREFIX = "dlx_";

  String QUEUE_MODE = "x-queue-mode";

  String QUEUE_LAZY = "lazy";

  String QUEUE_MAX_LENGTH = "x-max-length";

  String QUEUE_OVER_FLOW = "x-overflow";
}
