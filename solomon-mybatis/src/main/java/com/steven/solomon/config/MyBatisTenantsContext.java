package com.steven.solomon.config;

import com.steven.solomon.context.TenantContext;
import java.util.Map;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.springframework.stereotype.Component;

@Component
public class MyBatisTenantsContext extends TenantContext<PooledDataSourceFactory> {
}
