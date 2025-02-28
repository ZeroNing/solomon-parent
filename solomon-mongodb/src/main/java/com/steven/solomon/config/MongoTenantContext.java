package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoTenantContext extends TenantContext<SimpleMongoClientDatabaseFactory> {
}
