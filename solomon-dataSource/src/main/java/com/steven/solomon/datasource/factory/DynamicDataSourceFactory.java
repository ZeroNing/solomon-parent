package com.steven.solomon.datasource.factory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import com.steven.solomon.datasource.enums.DataSourcePoolTypeEnum;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.Druid;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.Hikari;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.Monitor;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.Security;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.SingleDataSourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.util.StringUtils;

/**
 * 数据源工厂。
 *
 * <p>根据配置创建Hikari或Druid连接池，并按数据库类型自动补齐驱动类。</p>
 */
public class DynamicDataSourceFactory {

  /**
   * 根据单租户配置创建数据源。
   *
   * @param properties 单租户数据源配置，包含连接池类型、数据库类型、JDBC地址和连接池参数
   * @return Hikari或Druid数据源
   * @throws DataSourceException 配置缺失、连接池类型不支持或数据库类型不支持时抛出
   */
  public DataSource createDataSource(SingleDataSourceProperties properties) throws DataSourceException {
    if (properties == null) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND);
    }
    DataSourcePoolTypeEnum poolType = properties.getPoolType();
    if (poolType == null) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_POOL_NOT_SUPPORTED, "null");
    }
    if (poolType == DataSourcePoolTypeEnum.HIKARI) {
      return createHikariDataSource(properties);
    }
    if (poolType == DataSourcePoolTypeEnum.DRUID) {
      return createDruidDataSource(properties);
    }
    throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_POOL_NOT_SUPPORTED, poolType);
  }

  private HikariDataSource createHikariDataSource(SingleDataSourceProperties properties)
      throws DataSourceException {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(properties.getUrl());
    config.setUsername(properties.getUsername());
    config.setPassword(properties.getPassword());
    config.setDriverClassName(resolveDriverClassName(properties));

    Hikari hikari = properties.getHikari();
    if (hikari != null) {
      if (hikari.getMaximumPoolSize() != null) {
        config.setMaximumPoolSize(hikari.getMaximumPoolSize());
      }
      if (hikari.getMinimumIdle() != null) {
        config.setMinimumIdle(hikari.getMinimumIdle());
      }
      if (hikari.getConnectionTimeout() != null) {
        config.setConnectionTimeout(hikari.getConnectionTimeout().toMillis());
      }
      if (hikari.getIdleTimeout() != null) {
        config.setIdleTimeout(hikari.getIdleTimeout().toMillis());
      }
      if (hikari.getMaxLifetime() != null) {
        config.setMaxLifetime(hikari.getMaxLifetime().toMillis());
      }
      if (hikari.getValidationTimeout() != null) {
        config.setValidationTimeout(hikari.getValidationTimeout().toMillis());
      }
      if (hikari.getLeakDetectionThreshold() != null) {
        config.setLeakDetectionThreshold(hikari.getLeakDetectionThreshold().toMillis());
      }
      if (StringUtils.hasText(hikari.getPoolName())) {
        config.setPoolName(hikari.getPoolName());
      }
      if (StringUtils.hasText(hikari.getConnectionTestQuery())) {
        config.setConnectionTestQuery(hikari.getConnectionTestQuery());
      }
      if (hikari.getReadOnly() != null) {
        config.setReadOnly(hikari.getReadOnly());
      }
    }
    return new HikariDataSource(config);
  }

  private DruidDataSource createDruidDataSource(SingleDataSourceProperties properties)
      throws DataSourceException {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setUrl(properties.getUrl());
    dataSource.setUsername(properties.getUsername());
    dataSource.setPassword(properties.getPassword());
    dataSource.setDriverClassName(resolveDriverClassName(properties));

    Druid druid = properties.getDruid();
    if (druid != null) {
      if (druid.getInitialSize() != null) {
        dataSource.setInitialSize(druid.getInitialSize());
      }
      if (druid.getMinIdle() != null) {
        dataSource.setMinIdle(druid.getMinIdle());
      }
      if (druid.getMaxActive() != null) {
        dataSource.setMaxActive(druid.getMaxActive());
      }
      if (druid.getMaxWait() != null) {
        dataSource.setMaxWait(druid.getMaxWait());
      }
      if (StringUtils.hasText(druid.getValidationQuery())) {
        dataSource.setValidationQuery(druid.getValidationQuery());
      }
      if (druid.getTestWhileIdle() != null) {
        dataSource.setTestWhileIdle(druid.getTestWhileIdle());
      }
      if (druid.getTestOnBorrow() != null) {
        dataSource.setTestOnBorrow(druid.getTestOnBorrow());
      }
      if (druid.getTestOnReturn() != null) {
        dataSource.setTestOnReturn(druid.getTestOnReturn());
      }
      if (druid.getPoolPreparedStatements() != null) {
        dataSource.setPoolPreparedStatements(druid.getPoolPreparedStatements());
      }
      if (druid.getMaxPoolPreparedStatementPerConnectionSize() != null) {
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(
            druid.getMaxPoolPreparedStatementPerConnectionSize());
      }
      if (druid.getTimeBetweenEvictionRunsMillis() != null) {
        dataSource.setTimeBetweenEvictionRunsMillis(druid.getTimeBetweenEvictionRunsMillis());
      }
      if (druid.getMinEvictableIdleTimeMillis() != null) {
        dataSource.setMinEvictableIdleTimeMillis(druid.getMinEvictableIdleTimeMillis());
      }
      if (druid.getMaxEvictableIdleTimeMillis() != null) {
        dataSource.setMaxEvictableIdleTimeMillis(druid.getMaxEvictableIdleTimeMillis());
      }
      if (druid.getKeepAlive() != null) {
        dataSource.setKeepAlive(druid.getKeepAlive());
      }
    }
    dataSource.setProxyFilters(buildDruidFilters(properties));
    Monitor monitor = properties.getMonitor();
    if (monitor != null) {
      dataSource.setUseGlobalDataSourceStat(monitor.isUseGlobalDataSourceStat());
    }
    return dataSource;
  }

  private List<Filter> buildDruidFilters(SingleDataSourceProperties properties) {
    List<Filter> filters = new ArrayList<>();
    Monitor monitor = properties.getMonitor();
    if (monitor != null && monitor.isEnabled()) {
      StatFilter statFilter = new StatFilter();
      statFilter.setMergeSql(monitor.isMergeSql());
      statFilter.setSlowSqlMillis(monitor.getSlowSqlMillis());
      statFilter.setLogSlowSql(monitor.isLogSlowSql());
      filters.add(statFilter);
    }

    Security security = properties.getSecurity();
    if (security != null && security.isEnabled()) {
      WallConfig wallConfig = new WallConfig();
      wallConfig.setMultiStatementAllow(security.isMultiStatementAllow());
      wallConfig.setNoneBaseStatementAllow(security.isNoneBaseStatementAllow());
      wallConfig.setStrictSyntaxCheck(security.isStrictSyntaxCheck());
      wallConfig.setCommentAllow(security.isCommentAllow());
      wallConfig.setConditionAndAlwayTrueAllow(security.isConditionAndAlwayTrueAllow());
      wallConfig.setConditionAndAlwayFalseAllow(security.isConditionAndAlwayFalseAllow());

      WallFilter wallFilter = new WallFilter();
      wallFilter.setConfig(wallConfig);
      wallFilter.setDbType(resolveDruidDbType(properties.getDatabaseType()));
      filters.add(wallFilter);
    }

    if (monitor != null && monitor.isSlf4jLogEnabled()) {
      filters.add(new Slf4jLogFilter());
    }
    return filters;
  }

  private String resolveDruidDbType(DataBaseTypeEnum databaseType) {
    if (databaseType == DataBaseTypeEnum.POSTGRESQL) {
      return "postgresql";
    }
    if (databaseType == DataBaseTypeEnum.SQL_SERVER) {
      return "sqlserver";
    }
    if (databaseType == DataBaseTypeEnum.ORACLE) {
      return "oracle";
    }
    if (databaseType == DataBaseTypeEnum.MARIADB) {
      return "mariadb";
    }
    return "mysql";
  }

  private String resolveDriverClassName(SingleDataSourceProperties properties) throws DataSourceException {
    if (StringUtils.hasText(properties.getDriverClassName())) {
      return properties.getDriverClassName();
    }
    DataBaseTypeEnum databaseType = properties.getDatabaseType();
    if (databaseType == null) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_DB_NOT_SUPPORTED, "null");
    }
    return databaseType.getDriverClassName();
  }
}
