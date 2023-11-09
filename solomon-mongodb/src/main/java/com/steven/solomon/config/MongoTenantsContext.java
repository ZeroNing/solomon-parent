package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoTenantsContext extends TenantContext<SimpleMongoClientDatabaseFactory> {

  @Override
  public SimpleMongoClientDatabaseFactory getFactory() {
    return THREAD_LOCAL.get();
  }

  @Override
  public void setFactory(String key) {
    THREAD_LOCAL.set(FACTORY_MAP.get(key));
  }

  @Override
  public void removeFactory() {
    THREAD_LOCAL.remove();
  }

  @Override
  public Map<String, SimpleMongoClientDatabaseFactory> getFactoryMap() {
    return FACTORY_MAP;
  }

  @Override
  public void setFactory(Map<String, SimpleMongoClientDatabaseFactory> factoryMap) {
    FACTORY_MAP.putAll(factoryMap);
  }

  @Override
  public void setFactory(String key, SimpleMongoClientDatabaseFactory factory) {
    FACTORY_MAP.put(key,factory);
  }
}
