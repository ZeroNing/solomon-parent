package com.steven.solomon.config;

import com.steven.solomon.config.profile.SqlProfile;
import com.steven.solomon.temple.SqlTemple;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import(value = SqlProfile.class)
public class SqlConfig {

    @Bean
    public DataSource hikariDataSource(SqlProfile sqlProfile) {
        SqlProfile.HikariData hikariData = sqlProfile.getHikariData();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(hikariData.getUrl());
        config.setUsername(hikariData.getUsername());
        config.setPassword(hikariData.getPassword());
        config.setDriverClassName(hikariData.getDriverClassName());
        config.setConnectionInitSql(hikariData.getConnectionInitSql());
        config.setValidationTimeout(hikariData.getValidationTimeout());
        config.setIdleTimeout(hikariData.getIdleTimeout());
        config.setLeakDetectionThreshold(hikariData.getLeakDetectionThreshold());
        config.setMaxLifetime(hikariData.getMaxLifetime());
        config.setMaximumPoolSize(hikariData.getMaxPoolSize());
        config.setMinimumIdle(hikariData.getMinIdle());
        config.setInitializationFailTimeout(hikariData.getInitializationFailTimeout());
        config.setConnectionInitSql(hikariData.getConnectionInitSql());
        config.setConnectionTestQuery(hikariData.getConnectionTestQuery());
        config.setKeepaliveTime(hikariData.getKeepaliveTime());
        config.setAutoCommit(hikariData.getIsAutoCommit());
        config.setReadOnly(hikariData.getIsReadOnly());
        config.setIsolateInternalQueries(hikariData.getIsIsolateInternalQueries());
        config.setRegisterMbeans(hikariData.getIsRegisterMbeans());
        config.setAllowPoolSuspension(hikariData.getIsAllowPoolSuspension());
        return new HikariDataSource(config);
    }

    @Bean
    public SqlTemple sqlTemple(DataSource dataSource) {
        return new SqlTemple(dataSource);
    }
}
