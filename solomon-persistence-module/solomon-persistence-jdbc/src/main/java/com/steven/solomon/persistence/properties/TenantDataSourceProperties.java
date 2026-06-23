package com.steven.solomon.persistence.properties;

import com.steven.solomon.persistence.enums.DataBaseTypeEnum;
import com.steven.solomon.persistence.enums.DataSourcePoolTypeEnum;
import com.steven.solomon.persistence.enums.SqlServerVersionEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 单个租户的数据源配置。
 */
public class TenantDataSourceProperties {

  @NotNull(message = "persistence.tenants[].pool-type must not be null")
  private DataSourcePoolTypeEnum poolType = DataSourcePoolTypeEnum.HIKARI;

  @NotNull(message = "persistence.tenants[].database-type must not be null")
  private DataBaseTypeEnum databaseType = DataBaseTypeEnum.MYSQL;

  @NotNull(message = "persistence.tenants[].sql-server-version must not be null")
  private SqlServerVersionEnum sqlServerVersion = SqlServerVersionEnum.SQL_SERVER_2012;

  @NotBlank(message = "persistence.tenants[].url must not be blank")
  private String url;

  private String username;

  private String password;

  private String driverClassName;

  @Valid
  @NotNull(message = "persistence.tenants[].hikari must not be null")
  private HikariPoolProperties hikari = new HikariPoolProperties();

  @Valid
  @NotNull(message = "persistence.tenants[].druid must not be null")
  private DruidPoolProperties druid = new DruidPoolProperties();

  @Valid
  @NotNull(message = "persistence.tenants[].monitor must not be null")
  private DruidMonitorProperties monitor = new DruidMonitorProperties();

  @Valid
  @NotNull(message = "persistence.tenants[].security must not be null")
  private DruidSecurityProperties security = new DruidSecurityProperties();

  public DataSourcePoolTypeEnum getPoolType() {
    return poolType;
  }

  public void setPoolType(DataSourcePoolTypeEnum poolType) {
    this.poolType = poolType;
  }

  public DataBaseTypeEnum getDatabaseType() {
    return databaseType;
  }

  public void setDatabaseType(DataBaseTypeEnum databaseType) {
    this.databaseType = databaseType;
  }

  public SqlServerVersionEnum getSqlServerVersion() {
    return sqlServerVersion;
  }

  public void setSqlServerVersion(SqlServerVersionEnum sqlServerVersion) {
    this.sqlServerVersion = sqlServerVersion;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public HikariPoolProperties getHikari() {
    return hikari;
  }

  public void setHikari(HikariPoolProperties hikari) {
    this.hikari = hikari;
  }

  public DruidPoolProperties getDruid() {
    return druid;
  }

  public void setDruid(DruidPoolProperties druid) {
    this.druid = druid;
  }

  public DruidMonitorProperties getMonitor() {
    return monitor;
  }

  public void setMonitor(DruidMonitorProperties monitor) {
    this.monitor = monitor;
  }

  public DruidSecurityProperties getSecurity() {
    return security;
  }

  public void setSecurity(DruidSecurityProperties security) {
    this.security = security;
  }
}
