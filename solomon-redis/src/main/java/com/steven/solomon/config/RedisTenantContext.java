package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Redis多租户上下文配置类。
 *
 * <p>继承自 {@link TenantContext}，以 {@link RedisConnectionFactory} 为资源类型，
 * 管理各租户的 Redis 连接工厂实例，支持多租户动态切换。</p>
 */
@Configuration
public class RedisTenantContext extends TenantContext<LettuceConnectionFactory> {
}
