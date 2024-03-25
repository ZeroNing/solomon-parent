package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoTenantsContext extends TenantContext<SimpleMongoClientDatabaseFactory> {
}
