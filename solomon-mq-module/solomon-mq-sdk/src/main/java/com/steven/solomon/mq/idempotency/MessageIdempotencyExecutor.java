package com.steven.solomon.mq.idempotency;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

public class MessageIdempotencyExecutor {

  private final MessageIdempotencyStore store;
  private final Duration processingTtl;
  private final Duration consumedTtl;

  public MessageIdempotencyExecutor(MessageIdempotencyStore store) {
    this(store, Duration.ofMinutes(10), Duration.ofDays(1));
  }

  public MessageIdempotencyExecutor(MessageIdempotencyStore store, Duration processingTtl, Duration consumedTtl) {
    this.store = Objects.requireNonNull(store, "store must not be null");
    this.processingTtl = processingTtl == null ? Duration.ofMinutes(10) : processingTtl;
    this.consumedTtl = consumedTtl == null ? Duration.ofDays(1) : consumedTtl;
  }

  public <T> T execute(String messageKey, Callable<T> task) throws Exception {
    if (!store.tryAcquire(messageKey, processingTtl)) {
      throw new DuplicateMessageException(messageKey);
    }
    try {
      T result = task.call();
      store.markConsumed(messageKey, consumedTtl);
      return result;
    } catch (Exception ex) {
      store.release(messageKey);
      throw ex;
    }
  }
}
