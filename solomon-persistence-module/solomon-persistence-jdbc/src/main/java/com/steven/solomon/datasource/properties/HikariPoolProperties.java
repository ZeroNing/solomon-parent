package com.steven.solomon.datasource.properties;

import java.time.Duration;

/**
 * HikariCP 连接池配置。
 */
public class HikariPoolProperties {

  private Integer maximumPoolSize;
  private Integer minimumIdle;
  private Duration connectionTimeout;
  private Duration idleTimeout;
  private Duration maxLifetime;
  private Duration validationTimeout;
  private Duration leakDetectionThreshold;
  private String poolName;
  private String connectionTestQuery;
  private Boolean readOnly;

  public Integer getMaximumPoolSize() { return maximumPoolSize; }
  public void setMaximumPoolSize(Integer value) { this.maximumPoolSize = value; }
  public Integer getMinimumIdle() { return minimumIdle; }
  public void setMinimumIdle(Integer value) { this.minimumIdle = value; }
  public Duration getConnectionTimeout() { return connectionTimeout; }
  public void setConnectionTimeout(Duration value) { this.connectionTimeout = value; }
  public Duration getIdleTimeout() { return idleTimeout; }
  public void setIdleTimeout(Duration value) { this.idleTimeout = value; }
  public Duration getMaxLifetime() { return maxLifetime; }
  public void setMaxLifetime(Duration value) { this.maxLifetime = value; }
  public Duration getValidationTimeout() { return validationTimeout; }
  public void setValidationTimeout(Duration value) { this.validationTimeout = value; }
  public Duration getLeakDetectionThreshold() { return leakDetectionThreshold; }
  public void setLeakDetectionThreshold(Duration value) { this.leakDetectionThreshold = value; }
  public String getPoolName() { return poolName; }
  public void setPoolName(String value) { this.poolName = value; }
  public String getConnectionTestQuery() { return connectionTestQuery; }
  public void setConnectionTestQuery(String value) { this.connectionTestQuery = value; }
  public Boolean getReadOnly() { return readOnly; }
  public void setReadOnly(Boolean value) { this.readOnly = value; }
}
