package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisTenantContext extends TenantContext<RedisConnectionFactory> {
}
