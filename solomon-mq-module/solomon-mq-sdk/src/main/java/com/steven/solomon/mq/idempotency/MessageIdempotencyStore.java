package com.steven.solomon.mq.idempotency;

import java.time.Duration;

public interface MessageIdempotencyStore {

  boolean tryAcquire(String key, Duration ttl);

  void markConsumed(String key, Duration ttl);

  void release(String key);
}
