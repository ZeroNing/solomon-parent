package com.steven.solomon.cache.redis.factory;

import java.time.Duration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Redis 连接工厂构建器，集中屏蔽 Spring Redis 连接参数组装细节。
 */
public class RedisConnectionFactoryBuilder {

  private static final String LOCALHOST = "localhost";

  private static final int DEFAULT_PORT = 6379;

  public LettuceConnectionFactory build(RedisProperties properties) {
    RedisProperties redisProperties = properties == null ? new RedisProperties() : properties;
    LettuceConnectionFactory factory = new LettuceConnectionFactory(
        standaloneConfiguration(redisProperties),
        clientConfiguration(redisProperties));
    factory.afterPropertiesSet();
    return factory;
  }

  private RedisStandaloneConfiguration standaloneConfiguration(RedisProperties properties) {
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
    configuration.setHostName(hasText(properties.getHost()) ? properties.getHost() : LOCALHOST);
    configuration.setPort(properties.getPort() > 0 ? properties.getPort() : DEFAULT_PORT);
    configuration.setDatabase(properties.getDatabase());
    if (hasText(properties.getUsername())) {
      configuration.setUsername(properties.getUsername());
    }
    if (hasText(properties.getPassword())) {
      configuration.setPassword(RedisPassword.of(properties.getPassword()));
    }
    return configuration;
  }

  private LettuceClientConfiguration clientConfiguration(RedisProperties properties) {
    LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
        LettuceClientConfiguration.builder();
    Duration timeout = properties.getTimeout();
    if (timeout != null) {
      builder.commandTimeout(timeout);
    }
    if (properties.getSsl() != null && properties.getSsl().isEnabled()) {
      builder.useSsl();
    }
    if (hasText(properties.getClientName())) {
      builder.clientName(properties.getClientName());
    }
    return builder.build();
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
