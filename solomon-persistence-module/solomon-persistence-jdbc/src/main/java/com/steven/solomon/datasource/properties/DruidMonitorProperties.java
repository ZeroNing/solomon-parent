package com.steven.solomon.datasource.properties;

/**
 * 单租户 Druid 监控配置。
 */
public class DruidMonitorProperties {

  private boolean enabled;
  private boolean mergeSql = true;
  private long slowSqlMillis = 3000;
  private boolean logSlowSql = true;
  private boolean useGlobalDataSourceStat;
  private boolean slf4jLogEnabled;

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean value) { this.enabled = value; }
  public boolean isMergeSql() { return mergeSql; }
  public void setMergeSql(boolean value) { this.mergeSql = value; }
  public long getSlowSqlMillis() { return slowSqlMillis; }
  public void setSlowSqlMillis(long value) { this.slowSqlMillis = value; }
  public boolean isLogSlowSql() { return logSlowSql; }
  public void setLogSlowSql(boolean value) { this.logSlowSql = value; }
  public boolean isUseGlobalDataSourceStat() { return useGlobalDataSourceStat; }
  public void setUseGlobalDataSourceStat(boolean value) { this.useGlobalDataSourceStat = value; }
  public boolean isSlf4jLogEnabled() { return slf4jLogEnabled; }
  public void setSlf4jLogEnabled(boolean value) { this.slf4jLogEnabled = value; }
}
