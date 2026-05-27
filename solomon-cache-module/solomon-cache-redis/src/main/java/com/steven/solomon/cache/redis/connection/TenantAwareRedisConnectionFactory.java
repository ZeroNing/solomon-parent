package com.steven.solomon.cache.redis.connection;

import com.steven.solomon.cache.redis.context.RedisCacheTenantContext;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;

/**
 * 支持线程级租户切换的 RedisConnectionFactory。
 */
public class TenantAwareRedisConnectionFactory implements RedisConnectionFactory {

  private final RedisCacheTenantContext tenantContext;

  private final RedisConnectionFactory defaultConnectionFactory;

  public TenantAwareRedisConnectionFactory(
      RedisCacheTenantContext tenantContext,
      RedisConnectionFactory defaultConnectionFactory) {
    this.tenantContext = tenantContext;
    this.defaultConnectionFactory = defaultConnectionFactory;
  }

  @Override
  public RedisConnection getConnection() {
    return delegate().getConnection();
  }

  @Override
  public RedisClusterConnection getClusterConnection() {
    return delegate().getClusterConnection();
  }

  @Override
  public RedisSentinelConnection getSentinelConnection() {
    return delegate().getSentinelConnection();
  }

  @Override
  public boolean getConvertPipelineAndTxResults() {
    return delegate().getConvertPipelineAndTxResults();
  }

  @Override
  public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
    return delegate().translateExceptionIfPossible(ex);
  }

  private RedisConnectionFactory delegate() {
    RedisConnectionFactory connectionFactory = tenantContext.getResource();
    return connectionFactory == null ? defaultConnectionFactory : connectionFactory;
  }
}
