package com.steven.solomon.cache.redis.context;

import com.steven.solomon.context.TenantContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Redis 缓存多租户连接上下文。
 *
 * <p>管理租户编码到 {@link RedisConnectionFactory} 的映射，
 * 支持线程级别动态切换 Redis 连接，实现缓存 key 不变但连接隔离的租户方案。
 * 对应 {@link com.steven.solomon.cache.key.CacheKeyMode#TENANT_SWITCH} 模式。</p>
 */
public class RedisCacheTenantContext extends TenantContext<RedisConnectionFactory> {
}
