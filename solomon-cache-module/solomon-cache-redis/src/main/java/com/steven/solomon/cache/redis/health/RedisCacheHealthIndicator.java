package com.steven.solomon.cache.redis.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public class RedisCacheHealthIndicator implements HealthIndicator {

  private final RedisConnectionFactory connectionFactory;

  public RedisCacheHealthIndicator(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public Health health() {
    try (RedisConnection connection = connectionFactory.getConnection()) {
      String pong = connection.ping();
      return Health.up()
          .withDetail("component", "solomon-cache-redis")
          .withDetail("ping", pong)
          .build();
    } catch (RuntimeException ex) {
      return Health.down(ex)
          .withDetail("component", "solomon-cache-redis")
          .build();
    }
  }
}
