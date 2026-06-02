package com.steven.solomon.datasource.properties;

/**
 * Druid 连接池配置。
 */
public class DruidPoolProperties {

  private Integer initialSize;
  private Integer minIdle;
  private Integer maxActive;
  private Long maxWait;
  private String validationQuery;
  private Boolean testWhileIdle;
  private Boolean testOnBorrow;
  private Boolean testOnReturn;
  private Boolean poolPreparedStatements;
  private Integer maxPoolPreparedStatementPerConnectionSize;
  private Long timeBetweenEvictionRunsMillis;
  private Long minEvictableIdleTimeMillis;
  private Long maxEvictableIdleTimeMillis;
  private Boolean keepAlive;

  public Integer getInitialSize() { return initialSize; }
  public void setInitialSize(Integer value) { this.initialSize = value; }
  public Integer getMinIdle() { return minIdle; }
  public void setMinIdle(Integer value) { this.minIdle = value; }
  public Integer getMaxActive() { return maxActive; }
  public void setMaxActive(Integer value) { this.maxActive = value; }
  public Long getMaxWait() { return maxWait; }
  public void setMaxWait(Long value) { this.maxWait = value; }
  public String getValidationQuery() { return validationQuery; }
  public void setValidationQuery(String value) { this.validationQuery = value; }
  public Boolean getTestWhileIdle() { return testWhileIdle; }
  public void setTestWhileIdle(Boolean value) { this.testWhileIdle = value; }
  public Boolean getTestOnBorrow() { return testOnBorrow; }
  public void setTestOnBorrow(Boolean value) { this.testOnBorrow = value; }
  public Boolean getTestOnReturn() { return testOnReturn; }
  public void setTestOnReturn(Boolean value) { this.testOnReturn = value; }
  public Boolean getPoolPreparedStatements() { return poolPreparedStatements; }
  public void setPoolPreparedStatements(Boolean value) { this.poolPreparedStatements = value; }
  public Integer getMaxPoolPreparedStatementPerConnectionSize() {
    return maxPoolPreparedStatementPerConnectionSize;
  }
  public void setMaxPoolPreparedStatementPerConnectionSize(Integer value) {
    this.maxPoolPreparedStatementPerConnectionSize = value;
  }
  public Long getTimeBetweenEvictionRunsMillis() { return timeBetweenEvictionRunsMillis; }
  public void setTimeBetweenEvictionRunsMillis(Long value) { this.timeBetweenEvictionRunsMillis = value; }
  public Long getMinEvictableIdleTimeMillis() { return minEvictableIdleTimeMillis; }
  public void setMinEvictableIdleTimeMillis(Long value) { this.minEvictableIdleTimeMillis = value; }
  public Long getMaxEvictableIdleTimeMillis() { return maxEvictableIdleTimeMillis; }
  public void setMaxEvictableIdleTimeMillis(Long value) { this.maxEvictableIdleTimeMillis = value; }
  public Boolean getKeepAlive() { return keepAlive; }
  public void setKeepAlive(Boolean value) { this.keepAlive = value; }
}
