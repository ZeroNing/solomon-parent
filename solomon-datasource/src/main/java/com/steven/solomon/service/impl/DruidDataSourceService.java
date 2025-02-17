package com.steven.solomon.service.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.steven.solomon.properties.DataSourceProperties;
import com.steven.solomon.service.DataSourceService;
import com.steven.solomon.verification.ValidateUtils;

import javax.sql.DataSource;

public class DruidDataSourceService implements DataSourceService {

    @Override
    public DataSource getDataSource(DataSourceProperties profile) throws Exception {
        DruidDataSource dataSource = new DruidDataSource();
        DataSourceProperties.DruidProperties druidProperties = profile.getDruidProperties();
        dataSource.setUrl(profile.getUrl());
        dataSource.setUsername(profile.getUsername());
        dataSource.setPassword(profile.getPassword());
        dataSource.setDriverClassName(profile.getDriverClassName());

        if(ValidateUtils.isNotEmpty(druidProperties)){
            // 配置连接池
            dataSource.setInitialSize(druidProperties.getInitialSize());
            dataSource.setMinIdle(druidProperties.getMinIdle());
            dataSource.setMaxActive(druidProperties.getMaxActive());
            dataSource.setMaxWait(druidProperties.getMaxWait());
            dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
            dataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());

            // 配置验证连接
            dataSource.setValidationQuery(druidProperties.getValidationQuery());
            dataSource.setValidationQueryTimeout(druidProperties.getValidationQueryTimeout());
            dataSource.setTestOnBorrow(druidProperties.getTestOnBorrow());
            dataSource.setTestWhileIdle(druidProperties.getTestWhileIdle());
            dataSource.setTestOnReturn(druidProperties.getTestOnReturn());

            // 配置高级选项
            dataSource.setPoolPreparedStatements(druidProperties.getPoolPreparedStatements());
            dataSource.setMaxOpenPreparedStatements(druidProperties.getMaxOpenPreparedStatements());
            dataSource.setLogAbandoned(druidProperties.getLogAbandoned());
            dataSource.setRemoveAbandoned(druidProperties.getRemoveAbandoned());
            dataSource.setRemoveAbandonedTimeout(druidProperties.getRemoveAbandonedTimeout());

            // 配置过滤器
            dataSource.setFilters(druidProperties.getFilters());

            // 配置其他项
            dataSource.setAsyncInit(druidProperties.getAsyncInit());
            dataSource.setMaxWaitThreadCount(druidProperties.getMaxWaitThreadCount());
            dataSource.setConnectionProperties(druidProperties.getLoginPassword());
            dataSource.setUseGlobalDataSourceStat(druidProperties.getUseGlobalDataSourceStat());
//        dataSource.setCreateDataSourceOnStartup(createDataSourceOnStartup);
//        dataSource.setStatViewServlet(statViewServlet);
//        dataSource.setLoginUsername(loginUsername);
//        dataSource.setLoginPassword(loginPassword);
//        dataSource.setUseLocalSessionState(useLocalSessionState);
        }

        dataSource.init();
        return dataSource;
    }
}
