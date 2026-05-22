package com.steven.solomon.datasource.properties;

import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import com.steven.solomon.datasource.enums.DataSourcePoolTypeEnum;
import com.steven.solomon.datasource.enums.SqlServerVersionEnum;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Solomon动态数据源配置。
 *
 * <p>配置结构按“租户 -> 数据源配置”组织，一个租户只对应一个数据源。</p>
 */
@ConfigurationProperties(prefix = "solomon.datasource")
public class SolomonDataSourceProperties {

  /** 是否启用动态数据源模块。 */
  private boolean enabled = true;

  /** 默认租户编码。 */
  private String defaultTenant = "default";

  /** 多租户数据源配置。 */
  private Map<String, SingleDataSourceProperties> tenants = new LinkedHashMap<>();

  /** 分页配置，用于控制普通分页何时自动切换为深度分页。 */
  private Page page = new Page();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDefaultTenant() {
    return defaultTenant;
  }

  public void setDefaultTenant(String defaultTenant) {
    this.defaultTenant = defaultTenant;
  }

  public Map<String, SingleDataSourceProperties> getTenants() {
    return tenants;
  }

  public void setTenants(Map<String, SingleDataSourceProperties> tenants) {
    this.tenants = tenants;
  }

  public Page getPage() {
    return page;
  }

  public void setPage(Page page) {
    this.page = page;
  }

  /**
   * 分页配置。
   */
  public static class Page {

    /** 是否自动把大页码分页切换为游标深度分页。 */
    private boolean autoSeekEnabled = true;

    /** 触发深度分页的最小页码，默认第500页开始。 */
    private int seekPageNo = 500;

    /** 触发深度分页的最小页大小，默认每页10条开始。 */
    private int seekPageSize = 10;

    /** 默认游标字段；调用方未传seekColumn时使用。 */
    private String defaultSeekColumn = "id";

    public boolean isAutoSeekEnabled() {
      return autoSeekEnabled;
    }

    public void setAutoSeekEnabled(boolean autoSeekEnabled) {
      this.autoSeekEnabled = autoSeekEnabled;
    }

    public int getSeekPageNo() {
      return seekPageNo;
    }

    public void setSeekPageNo(int seekPageNo) {
      this.seekPageNo = seekPageNo;
    }

    public int getSeekPageSize() {
      return seekPageSize;
    }

    public void setSeekPageSize(int seekPageSize) {
      this.seekPageSize = seekPageSize;
    }

    public String getDefaultSeekColumn() {
      return defaultSeekColumn;
    }

    public void setDefaultSeekColumn(String defaultSeekColumn) {
      this.defaultSeekColumn = defaultSeekColumn;
    }
  }

  /**
   * 单个数据源配置。
   */
  public static class SingleDataSourceProperties {

    /** 连接池类型，默认使用Hikari。 */
    private DataSourcePoolTypeEnum poolType = DataSourcePoolTypeEnum.HIKARI;

    /** 数据库类型，用于补齐驱动类。 */
    private DataBaseTypeEnum databaseType = DataBaseTypeEnum.MYSQL;

    /** SQL Server版本，仅databaseType为SQL_SERVER时生效。 */
    private SqlServerVersionEnum sqlServerVersion = SqlServerVersionEnum.SQL_SERVER_2012;

    /** JDBC连接地址。 */
    private String url;

    /** 数据库用户名。 */
    private String username;

    /** 数据库密码。 */
    private String password;

    /** 驱动类；为空时按databaseType自动推导。 */
    private String driverClassName;

    /** Hikari连接池扩展配置。 */
    private Hikari hikari = new Hikari();

    /** Druid连接池扩展配置。 */
    private Druid druid = new Druid();

    /** Druid监控配置，按租户数据源独立生效。 */
    private Monitor monitor = new Monitor();

    /** Druid安全配置，按租户数据源独立生效。 */
    private Security security = new Security();

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

    public Hikari getHikari() {
      return hikari;
    }

    public void setHikari(Hikari hikari) {
      this.hikari = hikari;
    }

    public Druid getDruid() {
      return druid;
    }

    public void setDruid(Druid druid) {
      this.druid = druid;
    }

    public Monitor getMonitor() {
      return monitor;
    }

    public void setMonitor(Monitor monitor) {
      this.monitor = monitor;
    }

    public Security getSecurity() {
      return security;
    }

    public void setSecurity(Security security) {
      this.security = security;
    }
  }

  /**
   * Hikari连接池配置。
   */
  public static class Hikari {

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

    public Integer getMaximumPoolSize() {
      return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
      this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getMinimumIdle() {
      return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
      this.minimumIdle = minimumIdle;
    }

    public Duration getConnectionTimeout() {
      return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
    }

    public Duration getIdleTimeout() {
      return idleTimeout;
    }

    public void setIdleTimeout(Duration idleTimeout) {
      this.idleTimeout = idleTimeout;
    }

    public Duration getMaxLifetime() {
      return maxLifetime;
    }

    public void setMaxLifetime(Duration maxLifetime) {
      this.maxLifetime = maxLifetime;
    }

    public Duration getValidationTimeout() {
      return validationTimeout;
    }

    public void setValidationTimeout(Duration validationTimeout) {
      this.validationTimeout = validationTimeout;
    }

    public Duration getLeakDetectionThreshold() {
      return leakDetectionThreshold;
    }

    public void setLeakDetectionThreshold(Duration leakDetectionThreshold) {
      this.leakDetectionThreshold = leakDetectionThreshold;
    }

    public String getPoolName() {
      return poolName;
    }

    public void setPoolName(String poolName) {
      this.poolName = poolName;
    }

    public String getConnectionTestQuery() {
      return connectionTestQuery;
    }

    public void setConnectionTestQuery(String connectionTestQuery) {
      this.connectionTestQuery = connectionTestQuery;
    }

    public Boolean getReadOnly() {
      return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
      this.readOnly = readOnly;
    }
  }

  /**
   * Druid连接池配置。
   */
  public static class Druid {

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

    public Integer getInitialSize() {
      return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
      this.initialSize = initialSize;
    }

    public Integer getMinIdle() {
      return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
      this.minIdle = minIdle;
    }

    public Integer getMaxActive() {
      return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
      this.maxActive = maxActive;
    }

    public Long getMaxWait() {
      return maxWait;
    }

    public void setMaxWait(Long maxWait) {
      this.maxWait = maxWait;
    }

    public String getValidationQuery() {
      return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
      this.validationQuery = validationQuery;
    }

    public Boolean getTestWhileIdle() {
      return testWhileIdle;
    }

    public void setTestWhileIdle(Boolean testWhileIdle) {
      this.testWhileIdle = testWhileIdle;
    }

    public Boolean getTestOnBorrow() {
      return testOnBorrow;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
      this.testOnBorrow = testOnBorrow;
    }

    public Boolean getTestOnReturn() {
      return testOnReturn;
    }

    public void setTestOnReturn(Boolean testOnReturn) {
      this.testOnReturn = testOnReturn;
    }

    public Boolean getPoolPreparedStatements() {
      return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
      this.poolPreparedStatements = poolPreparedStatements;
    }

    public Integer getMaxPoolPreparedStatementPerConnectionSize() {
      return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(
        Integer maxPoolPreparedStatementPerConnectionSize) {
      this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public Long getTimeBetweenEvictionRunsMillis() {
      return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
      this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Long getMinEvictableIdleTimeMillis() {
      return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
      this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public Long getMaxEvictableIdleTimeMillis() {
      return maxEvictableIdleTimeMillis;
    }

    public void setMaxEvictableIdleTimeMillis(Long maxEvictableIdleTimeMillis) {
      this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
    }

    public Boolean getKeepAlive() {
      return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
      this.keepAlive = keepAlive;
    }
  }

  /**
   * Druid监控配置。
   */
  public static class Monitor {

    /** 是否启用SQL统计监控。 */
    private boolean enabled = false;

    /** 是否合并相同SQL。 */
    private boolean mergeSql = true;

    /** 慢SQL阈值，单位毫秒。 */
    private long slowSqlMillis = 3000;

    /** 是否记录慢SQL日志。 */
    private boolean logSlowSql = true;

    /** 是否启用Druid全局数据源统计。 */
    private boolean useGlobalDataSourceStat = false;

    /** 是否启用Slf4j日志过滤器。 */
    private boolean slf4jLogEnabled = false;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isMergeSql() {
      return mergeSql;
    }

    public void setMergeSql(boolean mergeSql) {
      this.mergeSql = mergeSql;
    }

    public long getSlowSqlMillis() {
      return slowSqlMillis;
    }

    public void setSlowSqlMillis(long slowSqlMillis) {
      this.slowSqlMillis = slowSqlMillis;
    }

    public boolean isLogSlowSql() {
      return logSlowSql;
    }

    public void setLogSlowSql(boolean logSlowSql) {
      this.logSlowSql = logSlowSql;
    }

    public boolean isUseGlobalDataSourceStat() {
      return useGlobalDataSourceStat;
    }

    public void setUseGlobalDataSourceStat(boolean useGlobalDataSourceStat) {
      this.useGlobalDataSourceStat = useGlobalDataSourceStat;
    }

    public boolean isSlf4jLogEnabled() {
      return slf4jLogEnabled;
    }

    public void setSlf4jLogEnabled(boolean slf4jLogEnabled) {
      this.slf4jLogEnabled = slf4jLogEnabled;
    }
  }

  /**
   * Druid SQL防火墙安全配置。
   */
  public static class Security {

    /** 是否启用SQL防火墙。 */
    private boolean enabled = false;

    /** 是否允许多语句执行，默认关闭更安全。 */
    private boolean multiStatementAllow = false;

    /** 是否允许非基础语句，默认关闭更安全。 */
    private boolean noneBaseStatementAllow = false;

    /** 是否开启严格语法检查。 */
    private boolean strictSyntaxCheck = true;

    /** 是否允许SQL注释。 */
    private boolean commentAllow = false;

    /** 是否允许条件永真表达式。 */
    private boolean conditionAndAlwayTrueAllow = false;

    /** 是否允许条件永假表达式。 */
    private boolean conditionAndAlwayFalseAllow = false;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isMultiStatementAllow() {
      return multiStatementAllow;
    }

    public void setMultiStatementAllow(boolean multiStatementAllow) {
      this.multiStatementAllow = multiStatementAllow;
    }

    public boolean isNoneBaseStatementAllow() {
      return noneBaseStatementAllow;
    }

    public void setNoneBaseStatementAllow(boolean noneBaseStatementAllow) {
      this.noneBaseStatementAllow = noneBaseStatementAllow;
    }

    public boolean isStrictSyntaxCheck() {
      return strictSyntaxCheck;
    }

    public void setStrictSyntaxCheck(boolean strictSyntaxCheck) {
      this.strictSyntaxCheck = strictSyntaxCheck;
    }

    public boolean isCommentAllow() {
      return commentAllow;
    }

    public void setCommentAllow(boolean commentAllow) {
      this.commentAllow = commentAllow;
    }

    public boolean isConditionAndAlwayTrueAllow() {
      return conditionAndAlwayTrueAllow;
    }

    public void setConditionAndAlwayTrueAllow(boolean conditionAndAlwayTrueAllow) {
      this.conditionAndAlwayTrueAllow = conditionAndAlwayTrueAllow;
    }

    public boolean isConditionAndAlwayFalseAllow() {
      return conditionAndAlwayFalseAllow;
    }

    public void setConditionAndAlwayFalseAllow(boolean conditionAndAlwayFalseAllow) {
      this.conditionAndAlwayFalseAllow = conditionAndAlwayFalseAllow;
    }
  }
}
