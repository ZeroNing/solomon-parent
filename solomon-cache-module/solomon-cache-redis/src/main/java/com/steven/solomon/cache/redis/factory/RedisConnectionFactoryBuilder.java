package com.steven.solomon.cache.redis.factory;

import java.time.Duration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Redis 连接工厂构建器。
 *
 * <p>集中屏蔽 Spring Redis 连接参数组装细节，基于 {@link RedisProperties}
 * 创建 {@link LettuceConnectionFactory}，处理主机、端口、密码、超时和 SSL 等配置。</p>
 */
public class RedisConnectionFactoryBuilder {

  /** 默认主机地址。 */
  private static final String LOCALHOST = "localhost";

  /** 默认端口。 */
  private static final int DEFAULT_PORT = 6379;

  /**
   * 根据配置构建 Lettuce 连接工厂。
   *
   * @param properties Redis 连接配置
   * @return 初始化后的 LettuceConnectionFactory
   */
  public LettuceConnectionFactory build(RedisProperties properties) {
    RedisProperties redisProperties = properties == null ? new RedisProperties() : properties;
    LettuceConnectionFactory factory = new LettuceConnectionFactory(
        standaloneConfiguration(redisProperties),
        clientConfiguration(redisProperties));
    factory.afterPropertiesSet();
    return factory;
  }

  /** 构建单机 Redis 配置，含主机、端口、数据库、用户名和密码。 */
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

  /** 构建 Lettuce 客户端配置，含超时、SSL 和客户端名称。 */
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

  /** 判断字符串是否为非空白。 */
  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
