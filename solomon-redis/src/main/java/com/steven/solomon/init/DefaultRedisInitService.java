package com.steven.solomon.init;

import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.verification.ValidateUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

public class DefaultRedisInitService extends AbstractDataSourceInitService<RedisProperties, RedisTenantContext, LettuceConnectionFactory>{
    @Override
    public void init(String tenantCode, RedisProperties properties, RedisTenantContext context) throws Throwable {
        context.setFactory(tenantCode, initFactory(properties));
    }

    @Override
    public LettuceConnectionFactory initFactory(RedisProperties properties) throws Throwable {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        RedisProperties.Pool pool                    = properties.getLettuce().getPool();
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
        redisStandaloneConfiguration.setDatabase(properties.getDatabase());
        redisStandaloneConfiguration.setHostName(properties.getHost());
        redisStandaloneConfiguration.setPort(properties.getPort());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(properties.getPassword()));

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
