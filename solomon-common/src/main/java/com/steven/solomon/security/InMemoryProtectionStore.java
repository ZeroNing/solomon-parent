package com.steven.solomon.security;

import com.steven.solomon.security.ProtectionStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 进程内安全防护存储。
 *
 * <p>这个实现不依赖 Redis，适合单节点、本地开发和测试环境。
 * 多节点部署时，不同节点之间无法共享幂等 key、nonce 和限流计数，
 * 生产环境建议使用 Redis 或业务自定义的 {@link ProtectionStore} 实现。</p>
 */
public class InMemoryProtectionStore implements ProtectionStore {

  /**
   * 保存幂等 key、nonce 等只需要判断存在性的临时 key。
   */
  private final Map<String, Long> expiringKeys = new ConcurrentHashMap<>();

  /**
   * 保存固定窗口限流计数。
   */
  private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

  @Override
  public boolean putIfAbsent(String key, long ttlMillis) {
    long now = System.currentTimeMillis();
    cleanup(now);
    Long expireAt = expiringKeys.putIfAbsent(key, now + ttlMillis);
    if (expireAt == null) {
      return true;
    }
    if (expireAt <= now && expiringKeys.replace(key, expireAt, now + ttlMillis)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean allow(String key, int permits, long windowMillis) {
    long now = System.currentTimeMillis();
    cleanup(now);
    WindowCounter counter = counters.compute(key, (k, current) -> {
      if (current == null || current.expireAt <= now) {
        return new WindowCounter(now + windowMillis);
      }
      return current;
    });
    return counter.count.incrementAndGet() <= permits;
  }

  private void cleanup(long now) {
    expiringKeys.entrySet().removeIf(entry -> entry.getValue() <= now);
    counters.entrySet().removeIf(entry -> entry.getValue().expireAt <= now);
  }

  private static class WindowCounter {

    private final long expireAt;

    private final AtomicInteger count = new AtomicInteger();

    private WindowCounter(long expireAt) {
      this.expireAt = expireAt;
    }
  }
}
