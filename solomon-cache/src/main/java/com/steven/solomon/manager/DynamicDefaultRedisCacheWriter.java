package com.steven.solomon.manager;
import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class DynamicDefaultRedisCacheWriter implements RedisCacheWriter {

  private RedisConnectionFactory   connectionFactory;
  private Duration                 sleepTime;
  private CacheStatisticsCollector statistics;

  public static RedisCacheWriter nonLockingRedisCacheWriter(RedisConnectionFactory connectionFactory) {

    Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");

    return new DynamicDefaultRedisCacheWriter(connectionFactory);
  }

  /**
   * @param connectionFactory must not be {@literal null}.
   */
  DynamicDefaultRedisCacheWriter(RedisConnectionFactory connectionFactory) {
    this(connectionFactory, Duration.ZERO);
  }

  /**
   * @param connectionFactory must not be {@literal null}.
   * @param sleepTime sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
   *          to disable locking.
   */
  DynamicDefaultRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime) {
    this(connectionFactory, sleepTime, CacheStatisticsCollector.none());
  }

  /**
   * @param connectionFactory must not be {@literal null}.
   * @param sleepTime sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
   *          to disable locking.
   * @param cacheStatisticsCollector must not be {@literal null}.
   */
  DynamicDefaultRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime,
      CacheStatisticsCollector cacheStatisticsCollector) {

    Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
    Assert.notNull(sleepTime, "SleepTime must not be null!");
    Assert.notNull(cacheStatisticsCollector, "CacheStatisticsCollector must not be null!");

    this.connectionFactory = connectionFactory;
    this.sleepTime = sleepTime;
    this.statistics = cacheStatisticsCollector;
  }

  public RedisConnectionFactory getRedisConnectionFactory(){
    RedisConnectionFactory connectionFactory = SpringUtil.getBean(RedisTenantContext.class).getFactory();
    return ValidateUtils.isEmpty(connectionFactory) ? this.connectionFactory : connectionFactory;
  }


  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.RedisCacheWriter#put(java.lang.String, byte[], byte[], java.time.Duration)
   */
  @Override
  public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");
    Assert.notNull(value, "Value must not be null!");

    execute(name, connection -> {

      if (shouldExpireWithin(ttl)) {
        connection.set(key, value, Expiration.from(ttl.toMillis(), TimeUnit.MILLISECONDS), SetOption.upsert());
      } else {
        connection.set(key, value);
      }

      return "OK";
    });

    statistics.incPuts(name);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.RedisCacheWriter#get(java.lang.String, byte[])
   */
  @Override
  public byte[] get(String name, byte[] key) {

    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    byte[] result = execute(name, connection -> connection.get(key));

    statistics.incGets(name);

    if (result != null) {
      statistics.incHits(name);
    } else {
      statistics.incMisses(name);
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.RedisCacheWriter#putIfAbsent(java.lang.String, byte[], byte[], java.time.Duration)
   */
  @Override
  public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");
    Assert.notNull(value, "Value must not be null!");

    return execute(name, connection -> {

      if (isLockingCacheWriter()) {
        doLock(name, connection);
      }

      try {

        boolean put;

        if (shouldExpireWithin(ttl)) {
          put = connection.set(key, value, Expiration.from(ttl), SetOption.ifAbsent());
        } else {
          put = connection.setNX(key, value);
        }

        if (put) {
          statistics.incPuts(name);
          return null;
        }

        return connection.get(key);
      } finally {

        if (isLockingCacheWriter()) {
          doUnlock(name, connection);
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.RedisCacheWriter#remove(java.lang.String, byte[])
   */
  @Override
  public void remove(String name, byte[] key) {

    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    execute(name, connection -> connection.del(key));
    statistics.incDeletes(name);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.RedisCacheWriter#clean(java.lang.String, byte[])
   */
  @Override
  public void clean(String name, byte[] pattern) {

    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(pattern, "Pattern must not be null!");

    execute(name, connection -> {

      boolean wasLocked = false;

      try {

        if (isLockingCacheWriter()) {
          doLock(name, connection);
          wasLocked = true;
        }

        byte[][] keys = Optional.ofNullable(connection.keys(pattern)).orElse(Collections.emptySet())
            .toArray(new byte[0][]);

        if (keys.length > 0) {
          statistics.incDeletesBy(name, keys.length);
          connection.del(keys);
        }
      } finally {

        if (wasLocked && isLockingCacheWriter()) {
          doUnlock(name, connection);
        }
      }

      return "OK";
    });
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.CacheStatisticsProvider#getCacheStatistics(java.lang.String)
   */
  @Override
  public CacheStatistics getCacheStatistics(String cacheName) {
    return statistics.getCacheStatistics(cacheName);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.RedisCacheWriter#clearStatistics(java.lang.String)
   */
  @Override
  public void clearStatistics(String name) {
    statistics.reset(name);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.redis.cache.RedisCacheWriter#with(CacheStatisticsCollector)
   */
  @Override
  public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
    return new DynamicDefaultRedisCacheWriter(connectionFactory, sleepTime, cacheStatisticsCollector);
  }

  /**
   * Explicitly set a write lock on a cache.
   *
   * @param name the name of the cache to lock.
   */
  void lock(String name) {
    execute(name, connection -> doLock(name, connection));
  }

  /**
   * Explicitly remove a write lock from a cache.
   *
   * @param name the name of the cache to unlock.
   */
  void unlock(String name) {
    executeLockFree(connection -> doUnlock(name, connection));
  }

  private Boolean doLock(String name, RedisConnection connection) {
    return connection.setNX(createCacheLockKey(name), new byte[0]);
  }

  private Long doUnlock(String name, RedisConnection connection) {
    return connection.del(createCacheLockKey(name));
  }

  boolean doCheckLock(String name, RedisConnection connection) {
    return connection.exists(createCacheLockKey(name));
  }

  /**
   * @return {@literal true} if {@link RedisCacheWriter} uses locks.
   */
  private boolean isLockingCacheWriter() {
    return !sleepTime.isZero() && !sleepTime.isNegative();
  }

  private <T> T execute(String name, Function<RedisConnection, T> callback) {

    RedisConnection connection = getRedisConnectionFactory().getConnection();
    try {

      checkAndPotentiallyWaitUntilUnlocked(name, connection);
      return callback.apply(connection);
    } finally {
      connection.close();
    }
  }

  private void executeLockFree(Consumer<RedisConnection> callback) {

    RedisConnection connection = getRedisConnectionFactory().getConnection();

    try {
      callback.accept(connection);
    } finally {
      connection.close();
    }
  }

  private void checkAndPotentiallyWaitUntilUnlocked(String name, RedisConnection connection) {

    if (!isLockingCacheWriter()) {
      return;
    }

    long lockWaitTimeNs = System.nanoTime();
    try {

      while (doCheckLock(name, connection)) {
        Thread.sleep(sleepTime.toMillis());
      }
    } catch (InterruptedException ex) {

      // Re-interrupt current thread, to allow other participants to react.
      Thread.currentThread().interrupt();

      throw new PessimisticLockingFailureException(String.format("Interrupted while waiting to unlock cache %s", name),
          ex);
    } finally {
      statistics.incLockTime(name, System.nanoTime() - lockWaitTimeNs);
    }
  }

  private static boolean shouldExpireWithin(@Nullable Duration ttl) {
    return ttl != null && !ttl.isZero() && !ttl.isNegative();
  }

  private static byte[] createCacheLockKey(String name) {
    return (name + "~lock").getBytes(StandardCharsets.UTF_8);
  }
}
