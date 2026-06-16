package com.steven.solomon.persistence.properties;

import com.steven.solomon.persistence.enums.DataBaseTypeEnum;
import com.steven.solomon.persistence.enums.DataSourcePoolTypeEnum;
import com.steven.solomon.persistence.enums.SqlServerVersionEnum;

/**
 * 单个租户的数据源配置�? */
public class TenantDataSourceProperties {

  private DataSourcePoolTypeEnum poolType = DataSourcePoolTypeEnum.HIKARI;

  private DataBaseTypeEnum databaseType = DataBaseTypeEnum.MYSQL;

  private SqlServerVersionEnum sqlServerVersion = SqlServerVersionEnum.SQL_SERVER_2012;

  private String url;

  private String username;

  private String password;

  private String driverClassName;

  private HikariPoolProperties hikari = new HikariPoolProperties();

  private DruidPoolProperties druid = new DruidPoolProperties();

  private DruidMonitorProperties monitor = new DruidMonitorProperties();

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
