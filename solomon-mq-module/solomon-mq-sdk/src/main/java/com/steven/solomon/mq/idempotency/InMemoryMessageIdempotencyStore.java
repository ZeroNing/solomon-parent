package com.steven.solomon.mq.idempotency;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMessageIdempotencyStore implements MessageIdempotencyStore {

  private final Map<String, Entry> entries = new ConcurrentHashMap<>();

  @Override
  public boolean tryAcquire(String key, Duration ttl) {
    long expiresAt = expiresAt(ttl);
    Entry next = new Entry(State.PROCESSING, expiresAt);
    Entry result = entries.compute(key, (ignored, existing) -> {
      if (existing == null || existing.isExpired()) {
        return next;
      }
      return existing;
    });
    return result == next;
  }

  @Override
  public void markConsumed(String key, Duration ttl) {
    entries.put(key, new Entry(State.CONSUMED, expiresAt(ttl)));
  }

  @Override
  public void release(String key) {
    entries.computeIfPresent(key, (ignored, existing) ->
        existing.state() == State.PROCESSING ? null : existing);
  }

  public int size() {
    entries.entrySet().removeIf(entry -> entry.getValue().isExpired());
    return entries.size();
  }

  private long expiresAt(Duration ttl) {
    long ttlMillis = ttl == null ? 0 : Math.max(0, ttl.toMillis());
    return ttlMillis == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + ttlMillis;
  }

  private enum State {
    PROCESSING,
    CONSUMED
  }

  private record Entry(State state, long expiresAt) {

    boolean isExpired() {
      return System.currentTimeMillis() >= expiresAt;
    }
  }
}
