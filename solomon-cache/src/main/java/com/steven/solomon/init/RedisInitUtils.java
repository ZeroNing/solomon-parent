package com.steven.solomon.init;

import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.verification.ValidateUtils;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

public class RedisInitUtils {

  public static void init(String tenantCode,RedisProperties properties,RedisTenantContext context) {
    context.setFactory(tenantCode, initConnectionFactory(properties));
  }

  public static void init(Map<String,RedisProperties> propertiesMap,RedisTenantContext context) {
    for(Entry<String,RedisProperties> entry : propertiesMap.entrySet()){
      init(entry.getKey(),entry.getValue(),context);
    }
  }

  public static LettuceConnectionFactory initConnectionFactory(RedisProperties redisProperties) {
    GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
    Pool                    pool                    = redisProperties.getLettuce().getPool();
    if (ValidateUtils.isNotEmpty(pool)) {
      genericObjectPoolConfig.setMaxIdle(ValidateUtils.getOrDefault(pool.getMaxIdle(), 0));
      genericObjectPoolConfig.setMinIdle(ValidateUtils.getOrDefault(pool.getMinIdle(), 0));
      genericObjectPoolConfig.setMaxTotal(ValidateUtils.getOrDefault(pool.getMaxActive(), 8));
      genericObjectPoolConfig.setMaxWaitMillis(ValidateUtils.getOrDefault(pool.getMaxWait().toMillis(), -1L));
      genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(
          ValidateUtils.getOrDefault(ValidateUtils.getOrDefault(pool.getTimeBetweenEvictionRuns(), Duration
              .ofMillis(60L)).toMillis(), 60L));
    }
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());
    redisStandaloneConfiguration.setHostName(redisProperties.getHost());
    redisStandaloneConfiguration.setPort(redisProperties.getPort());
    redisStandaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));

    LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
//        .commandTimeout(ValidateUtils.getOrDefault(redisProperties.getTimeout(),Duration.ofMillis(60L)))
//        .shutdownTimeout(ValidateUtils.getOrDefault(redisProperties.getLettuce().getShutdownTimeout(),Duration.ofMillis(100)))
        .poolConfig(genericObjectPoolConfig)
        .build();
    LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
    factory.afterPropertiesSet();
    return factory;
  }
}
