package com.steven.solomon.service.impl;

import com.steven.solomon.code.SqlErrorCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.properties.DataSourceProperties;
import com.steven.solomon.service.DataSourceService;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class HikariCPDataSourceService implements DataSourceService {

    @Override
    public DataSource getDataSource(DataSourceProperties profile) throws Exception {
        DataSourceProperties.HikariCPProperties hikariData = profile.getHikariCP();
        if(ValidateUtils.isEmpty(hikariData)){
            throw new BaseException(I18nUtils.getErrorMessage(SqlErrorCode.HIKARI_DATA_PROFILE_IS_NULL));
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(profile.getUrl());
        config.setUsername(profile.getUsername());
        config.setPassword(profile.getPassword());
        config.setDriverClassName(profile.getDriverClassName());
        if(ValidateUtils.isNotEmpty(config)){
            config.setValidationTimeout(hikariData.getValidationTimeout());
            config.setConnectionTimeout(hikariData.getConnectionTimeout());
            config.setPoolName(hikariData.getPoolName());
            config.setMaximumPoolSize(hikariData.getMaxPoolSize());
            config.setAutoCommit(hikariData.getAutoCommit());
            config.setReadOnly(hikariData.getReadOnly());

            config.setConnectionInitSql(hikariData.getConnectionInitSql());
            config.setIdleTimeout(hikariData.getIdleTimeout());
            config.setLeakDetectionThreshold(hikariData.getLeakDetectionThreshold());
            config.setMaxLifetime(hikariData.getMaxLifetime());
            config.setMinimumIdle(hikariData.getMinIdle());
            config.setInitializationFailTimeout(hikariData.getInitializationFailTimeout());
            config.setConnectionInitSql(hikariData.getConnectionInitSql());
            config.setConnectionTestQuery(hikariData.getConnectionTestQuery());
            config.setKeepaliveTime(hikariData.getKeepaliveTime());
            config.setIsolateInternalQueries(hikariData.getIsIsolateInternalQueries());
            config.setRegisterMbeans(hikariData.getIsRegisterMbeans());
            config.setAllowPoolSuspension(hikariData.getIsAllowPoolSuspension());
            if(ValidateUtils.isNotEmpty(hikariData.getDataSourceProperties())){
                config.setDataSourceProperties(hikariData.getDataSourceProperties());
            }
        }
        return new HikariDataSource(config);
    }
}
