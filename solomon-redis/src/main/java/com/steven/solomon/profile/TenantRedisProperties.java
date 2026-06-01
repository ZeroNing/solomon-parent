package com.steven.solomon.profile;

import com.steven.solomon.context.AbstractTenantProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tenant-specific Redis connection properties under {@code spring.redis.tenant}.
 */
@ConfigurationProperties(prefix = "spring.redis")
public class TenantRedisProperties extends AbstractTenantProperties<RedisProperties> {
}
