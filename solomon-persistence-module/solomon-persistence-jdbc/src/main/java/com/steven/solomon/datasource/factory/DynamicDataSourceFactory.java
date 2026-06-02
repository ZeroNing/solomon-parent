package com.steven.solomon.datasource.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import com.steven.solomon.datasource.enums.DataSourcePoolTypeEnum;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.properties.DruidMonitorProperties;
import com.steven.solomon.datasource.properties.DruidPoolProperties;
import com.steven.solomon.datasource.properties.DruidSecurityProperties;
import com.steven.solomon.datasource.properties.HikariPoolProperties;
import com.steven.solomon.datasource.properties.TenantDataSourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.sql.DataSource;

/**
 * 动态数据源工厂。
 *
 * <p>职责只做一件事：把租户配置安全地转换成连接池实例。数据库类型、驱动、校验 SQL、
 * Druid 防火墙 dbType 都从 {@link DataBaseTypeEnum} 统一解析，避免不同连接池各写一套规则。</p>
 */
public class DynamicDataSourceFactory {

  /**
   * 根据单租户配置创建数据源。
   *
   * @param properties 单租户数据源配置，包含连接池类型、数据库类型、JDBC 地址和连接池参数
   * @return Hikari 或 Druid 数据源
   * @throws DataSourceException 配置缺失、连接池类型不支持或数据库类型不支持时抛出
   */
  public DataSource createDataSource(TenantDataSourceProperties properties) throws DataSourceException {
    requireDataSourceConfig(properties);
    DataSourcePoolTypeEnum poolType = resolvePoolType(properties);
    if (poolType == DataSourcePoolTypeEnum.HIKARI) {
      return createHikariDataSource(properties);
    }
    if (poolType == DataSourcePoolTypeEnum.DRUID) {
      return createDruidDataSource(properties);
    }
    throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_POOL_NOT_SUPPORTED, poolType);
  }

  private HikariDataSource createHikariDataSource(TenantDataSourceProperties properties)
      throws DataSourceException {
    DataBaseTypeEnum databaseType = resolveDatabaseType(properties);
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(properties.getUrl());
    config.setUsername(properties.getUsername());
    config.setPassword(properties.getPassword());
    config.setDriverClassName(resolveDriverClassName(properties, databaseType));

    HikariPoolProperties hikari = ObjectUtil.defaultIfNull(properties.getHikari(),
        new HikariPoolProperties());
    setIfNotEmpty(hikari.getMaximumPoolSize(), config::setMaximumPoolSize);
    setIfNotEmpty(hikari.getMinimumIdle(), config::setMinimumIdle);
    setDurationIfNotEmpty(hikari.getConnectionTimeout(), config::setConnectionTimeout);
    setDurationIfNotEmpty(hikari.getIdleTimeout(), config::setIdleTimeout);
    setDurationIfNotEmpty(hikari.getMaxLifetime(), config::setMaxLifetime);
    setDurationIfNotEmpty(hikari.getValidationTimeout(), config::setValidationTimeout);
    setDurationIfNotEmpty(hikari.getLeakDetectionThreshold(), config::setLeakDetectionThreshold);
    setIfNotBlank(hikari.getPoolName(), config::setPoolName);
    setIfNotBlank(
        ObjectUtil.defaultIfNull(hikari.getConnectionTestQuery(), databaseType.getValidationQuery()),
        config::setConnectionTestQuery);
    setIfNotEmpty(hikari.getReadOnly(), config::setReadOnly);
    return new HikariDataSource(config);
  }

  private DruidDataSource createDruidDataSource(TenantDataSourceProperties properties)
      throws DataSourceException {
    DataBaseTypeEnum databaseType = resolveDatabaseType(properties);
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setUrl(properties.getUrl());
    dataSource.setUsername(properties.getUsername());
    dataSource.setPassword(properties.getPassword());
    dataSource.setDriverClassName(resolveDriverClassName(properties, databaseType));
    dataSource.setDbType(databaseType.getDruidDbType());

    DruidPoolProperties druid = ObjectUtil.defaultIfNull(properties.getDruid(),
        new DruidPoolProperties());
    setIfNotEmpty(druid.getInitialSize(), dataSource::setInitialSize);
    setIfNotEmpty(druid.getMinIdle(), dataSource::setMinIdle);
    setIfNotEmpty(druid.getMaxActive(), dataSource::setMaxActive);
    setIfNotEmpty(druid.getMaxWait(), dataSource::setMaxWait);
    setIfNotBlank(
        ObjectUtil.defaultIfNull(druid.getValidationQuery(), databaseType.getValidationQuery()),
        dataSource::setValidationQuery);
    setIfNotEmpty(druid.getTestWhileIdle(), dataSource::setTestWhileIdle);
    setIfNotEmpty(druid.getTestOnBorrow(), dataSource::setTestOnBorrow);
    setIfNotEmpty(druid.getTestOnReturn(), dataSource::setTestOnReturn);
    setIfNotEmpty(druid.getPoolPreparedStatements(), dataSource::setPoolPreparedStatements);
    setIfNotEmpty(
        druid.getMaxPoolPreparedStatementPerConnectionSize(),
        dataSource::setMaxPoolPreparedStatementPerConnectionSize);
    setIfNotEmpty(druid.getTimeBetweenEvictionRunsMillis(),
        dataSource::setTimeBetweenEvictionRunsMillis);
    setIfNotEmpty(druid.getMinEvictableIdleTimeMillis(),
        dataSource::setMinEvictableIdleTimeMillis);
    setIfNotEmpty(druid.getMaxEvictableIdleTimeMillis(),
        dataSource::setMaxEvictableIdleTimeMillis);
    setIfNotEmpty(druid.getKeepAlive(), dataSource::setKeepAlive);

    dataSource.setProxyFilters(buildDruidFilters(properties, databaseType));
    DruidMonitorProperties monitor = properties.getMonitor();
    if (ObjectUtil.isNotEmpty(monitor)) {
      dataSource.setUseGlobalDataSourceStat(monitor.isUseGlobalDataSourceStat());
    }
    return dataSource;
  }

  private List<Filter> buildDruidFilters(
      TenantDataSourceProperties properties,
      DataBaseTypeEnum databaseType) {
    List<Filter> filters = new ArrayList<>();
    DruidMonitorProperties monitor = properties.getMonitor();
    if (ObjectUtil.isNotEmpty(monitor) && monitor.isEnabled()) {
      StatFilter statFilter = new StatFilter();
      statFilter.setMergeSql(monitor.isMergeSql());
      statFilter.setSlowSqlMillis(monitor.getSlowSqlMillis());
      statFilter.setLogSlowSql(monitor.isLogSlowSql());
      filters.add(statFilter);
    }

    DruidSecurityProperties security = properties.getSecurity();
    if (ObjectUtil.isNotEmpty(security) && security.isEnabled()) {
      WallFilter wallFilter = new WallFilter();
      wallFilter.setConfig(buildWallConfig(security));
      wallFilter.setDbType(databaseType.getDruidDbType());
      filters.add(wallFilter);
    }

    if (ObjectUtil.isNotEmpty(monitor) && monitor.isSlf4jLogEnabled()) {
      filters.add(new Slf4jLogFilter());
    }
    return filters;
  }

  private WallConfig buildWallConfig(DruidSecurityProperties security) {
    WallConfig wallConfig = new WallConfig();
    wallConfig.setMultiStatementAllow(security.isMultiStatementAllow());
    wallConfig.setNoneBaseStatementAllow(security.isNoneBaseStatementAllow());
    wallConfig.setStrictSyntaxCheck(security.isStrictSyntaxCheck());
    wallConfig.setCommentAllow(security.isCommentAllow());
    wallConfig.setConditionAndAlwayTrueAllow(security.isConditionAndAlwayTrueAllow());
    wallConfig.setConditionAndAlwayFalseAllow(security.isConditionAndAlwayFalseAllow());
    return wallConfig;
  }

  private void requireDataSourceConfig(TenantDataSourceProperties properties)
      throws DataSourceException {
    if (ObjectUtil.isEmpty(properties)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND);
    }
    if (StrUtil.isBlank(properties.getUrl())) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND, "url");
    }
  }

  private DataSourcePoolTypeEnum resolvePoolType(TenantDataSourceProperties properties)
      throws DataSourceException {
    DataSourcePoolTypeEnum poolType = properties.getPoolType();
    if (ObjectUtil.isEmpty(poolType)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_POOL_NOT_SUPPORTED, "null");
    }
    return poolType;
  }

  private DataBaseTypeEnum resolveDatabaseType(TenantDataSourceProperties properties)
      throws DataSourceException {
    DataBaseTypeEnum databaseType = ObjectUtil.defaultIfNull(
        properties.getDatabaseType(),
        DataBaseTypeEnum.fromJdbcUrl(properties.getUrl()));
    if (ObjectUtil.isEmpty(databaseType)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_DB_NOT_SUPPORTED,
          properties.getUrl());
    }
    return databaseType;
  }

  private String resolveDriverClassName(
      TenantDataSourceProperties properties,
      DataBaseTypeEnum databaseType) {
    return StrUtil.isNotBlank(properties.getDriverClassName())
        ? properties.getDriverClassName()
        : databaseType.getDriverClassName();
  }

  private <T> void setIfNotEmpty(T value, Consumer<T> setter) {
    if (ObjectUtil.isNotEmpty(value)) {
      setter.accept(value);
    }
  }

  private void setIfNotBlank(String value, Consumer<String> setter) {
    if (StrUtil.isNotBlank(value)) {
      setter.accept(value);
    }
  }

  private void setDurationIfNotEmpty(Duration value, Consumer<Long> setter) {
    if (ObjectUtil.isNotEmpty(value)) {
      setter.accept(value.toMillis());
    }
  }
}
