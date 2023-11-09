package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import java.util.Map;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.springframework.stereotype.Component;

@Component
public class MyBatisTenantsContext extends TenantContext<PooledDataSourceFactory> {

  @Override
  public PooledDataSourceFactory getFactory() {
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
  public Map<String, PooledDataSourceFactory> getFactoryMap() {
    return FACTORY_MAP;
  }

  @Override
  public void setFactory(Map<String, PooledDataSourceFactory> factoryMap) {
    FACTORY_MAP.putAll(factoryMap);
  }

  @Override
  public void setFactory(String key, PooledDataSourceFactory factory) {
    FACTORY_MAP.put(key, factory);
  }
}
