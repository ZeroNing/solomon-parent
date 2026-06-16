package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongoDB多租户上下文配置类。
 *
 * <p>继承自 {@link TenantContext}，以 {@link SimpleMongoClientDatabaseFactory} 为资源类型，
 * 管理各租户的MongoDB数据库连接工厂实例，支持多租户动态切换。</p>
 */
@Configuration
public class MongoTenantContext extends TenantContext<SimpleMongoClientDatabaseFactory> {
}
