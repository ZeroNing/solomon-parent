package com.steven.solomon.cache.redis.context;

import com.steven.solomon.context.TenantContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Redis 缓存多租户连接上下文。
 */
public class RedisCacheTenantContext extends TenantContext<RedisConnectionFactory> {
}
