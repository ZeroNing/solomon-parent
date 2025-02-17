package com.steven.solomon.init;

import com.steven.solomon.code.SqlErrorCode;
import com.steven.solomon.config.DataSourceTenantContext;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.pojo.enums.ConnectionPoolTypeEnum;
import com.steven.solomon.properties.DataSourceProperties;
import com.steven.solomon.service.impl.DruidDataSourceService;
import com.steven.solomon.service.impl.HikariCPDataSourceService;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;

import javax.sql.DataSource;

public class DefaultDataSourceTenantInitService extends AbstractDataSourceInitService<DataSourceProperties, DataSourceTenantContext, DataSource>{

    @Override
    public void init(String tenantCode, DataSourceProperties properties, DataSourceTenantContext context) throws Throwable {
        DataSource dataSource = initFactory(properties);
        context.setFactory(tenantCode, dataSource);
    }

    @Override
    public DataSource initFactory(DataSourceProperties properties) throws Throwable {
        ConnectionPoolTypeEnum connectionPool = properties.getConnectionPoolType();
        if(ValidateUtils.isEmpty(connectionPool)){
            log.info("连接池类型为空,自动默认为:Druid");
            connectionPool = ConnectionPoolTypeEnum.DRUID;
        }
        DataSource dataSource = null;
        switch (connectionPool) {
            case DRUID:
                dataSource = new DruidDataSourceService().getDataSource(properties);
            case HIKARICP:
                dataSource = new HikariCPDataSourceService().getDataSource(properties);
            default:
                throw new BaseException(I18nUtils.getErrorMessage(SqlErrorCode.CONNECTION_POOL_TYPE_NO_MATCH,connectionPool.getName()));
        }
    }
}
