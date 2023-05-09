package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.properties.TenantMongoProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.stereotype.Component;

@Component
public class MongoTenantsContext extends TenantContext<SimpleMongoClientDatabaseFactory,TenantMongoProperties> {

  private  ThreadLocal<SimpleMongoClientDatabaseFactory> MONGO_DB_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

  private  Map<String,SimpleMongoClientDatabaseFactory> MONGO_FACTORY_MAP = new ConcurrentHashMap<>();

  private Map<String,TenantMongoProperties> mongoClientMap = new ConcurrentHashMap<>();

  private Map<String,Class<?>> CAPPED_COLLECTION_NAME_MAP = new HashMap<>();

  @Override
  public SimpleMongoClientDatabaseFactory getFactory() {
    return MONGO_DB_FACTORY_THREAD_LOCAL.get();
  }

  @Override
  public void setFactory(String key) {
    MONGO_DB_FACTORY_THREAD_LOCAL.set(MONGO_FACTORY_MAP.get(key));
  }

  @Override
  public void removeFactory() {
    MONGO_DB_FACTORY_THREAD_LOCAL.remove();
  }

  @Override
  public Map<String, SimpleMongoClientDatabaseFactory> getFactoryMap() {
    return MONGO_FACTORY_MAP;
  }

  @Override
  public void setFactory(Map<String, SimpleMongoClientDatabaseFactory> factoryMap) {
    MONGO_FACTORY_MAP.putAll(factoryMap);
  }

  @Override
  public void setFactory(String key, SimpleMongoClientDatabaseFactory factory) {
    MONGO_FACTORY_MAP.put(key,factory);
  }

  @Override
  public Map<String, TenantMongoProperties> getPropertiesMap() {
    return mongoClientMap;
  }

  @Override
  public void setProperties(String key, TenantMongoProperties properties) {
    mongoClientMap.put(key, properties);
  }

  @Override
  public void setProperties(Map<String, TenantMongoProperties> propertiesMap) {
    mongoClientMap.putAll(propertiesMap);
  }

}
