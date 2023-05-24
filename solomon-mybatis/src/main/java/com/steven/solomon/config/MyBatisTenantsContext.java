package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.properties.TenantDataSourceProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.springframework.stereotype.Component;

@Component
public class MyBatisTenantsContext extends TenantContext<PooledDataSourceFactory, TenantDataSourceProperties> {

  private  ThreadLocal<PooledDataSourceFactory> MYBATIS_DB_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

  private  Map<String,PooledDataSourceFactory> MYBATIS_FACTORY_MAP = new ConcurrentHashMap<>();

  @Override
  public PooledDataSourceFactory getFactory() {
    return MYBATIS_DB_FACTORY_THREAD_LOCAL.get();
  }

  @Override
  public void setFactory(String key) {
    MYBATIS_DB_FACTORY_THREAD_LOCAL.set(MYBATIS_FACTORY_MAP.get(key));
  }

  @Override
  public void removeFactory() {
    MYBATIS_DB_FACTORY_THREAD_LOCAL.remove();
  }

  @Override
  public Map<String, PooledDataSourceFactory> getFactoryMap() {
    return MYBATIS_FACTORY_MAP;
  }

  @Override
  public void setFactory(Map<String, PooledDataSourceFactory> factoryMap) {
    MYBATIS_FACTORY_MAP.putAll(factoryMap);
  }

  @Override
  public void setFactory(String key, PooledDataSourceFactory factory) {
    MYBATIS_FACTORY_MAP.put(key, factory);
  }
}
