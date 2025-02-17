package com.steven.solomon.properties;

import com.steven.solomon.pojo.enums.ConnectionPoolTypeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Properties;

public class DataSourceProperties {

    private ConnectionPoolTypeEnum connectionPoolType = ConnectionPoolTypeEnum.DRUID;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 数据库地址
     */
    private String url;

    /**
     * 数据库账号
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 数据库驱动类的完全限定名
     */
    private String driverClassName;

    private HikariCPProperties hikariCP;

    private DruidProperties druidProperties;

    public static class DruidProperties implements Serializable {
        // 连接池的配置项
        /** 连接池初始化时创建的连接数，默认 5 */
        private int initialSize = 5;

        /** 最小空闲连接数，默认 5 */
        private int minIdle = 5;

        /** 最大活动连接数，默认 20 */
        private int maxActive = 20;

        /** 获取连接的最大等待时间，单位：毫秒，默认 60000 毫秒 (60秒) */
        private int maxWait = 60000;

        /** 空闲连接检测的时间间隔，单位：毫秒，默认 60000 毫秒 (60秒) */
        private int timeBetweenEvictionRunsMillis = 60000;

        /** 空闲连接最小回收时间，单位：毫秒，默认 300000 毫秒 (5分钟) */
        private int minEvictableIdleTimeMillis = 300000;

        // 验证连接的配置项
        /** 验证连接是否有效的 SQL 查询语句，默认 SELECT 1 */
        private String validationQuery = "SELECT 1";

        /** 验证连接的超时时间，单位：秒，默认 3 秒 */
        private int validationQueryTimeout = 3;

        /** 获取连接时是否进行验证，默认启用验证 */
        private boolean testOnBorrow = true;

        /** 空闲时是否进行验证，默认启用验证 */
        private boolean testWhileIdle = true;

        /** 归还连接时是否进行验证，默认不进行验证 */
        private boolean testOnReturn = false;

        // 高级配置项
        /** 是否缓存 PreparedStatement，默认启用 */
        private boolean poolPreparedStatements = true;

        /** 最大打开的 PreparedStatement 数量，默认 20 */
        private int maxOpenPreparedStatements = 20;

        /** 是否启用连接泄漏日志，默认启用 */
        private boolean logAbandoned = true;

        /** 是否回收连接泄漏，默认启用 */
        private boolean removeAbandoned = true;

        /** 回收泄漏连接的超时时间，单位：秒，默认 300 秒 */
        private int removeAbandonedTimeout = 300;

        /** Druid 过滤器（如 stat、log4j），默认启用统计和日志过滤器 */
        private String filters = "stat,log4j";

        // 其他配置项
        /** 是否异步初始化 Druid 数据源，默认不启用 */
        private boolean asyncInit = false;

        /** 等待连接的最大线程数，默认 200 */
        private int maxWaitThreadCount = 200;

        /** 是否启用全局数据源统计，默认不启用 */
        private boolean useGlobalDataSourceStat = false;

        /** 是否在应用启动时创建数据源，默认不创建 */
        private boolean createDataSourceOnStartup = false;

        /** Druid 的统计视图 URL，默认是 "/druid/stat" */
        private String statViewServlet = "/druid/stat";

        /** Druid Web 监控界面的访问用户名，默认 "admin" */
        private String loginUsername = "admin";

        /** Druid Web 监控界面的访问密码，默认 "admin" */
        private String loginPassword = "admin";

        /** 是否启用本地会话状态，默认不启用 */
        private boolean useLocalSessionState = false;

        public boolean getAsyncInit() {
            return asyncInit;
        }

        public void setAsyncInit(boolean asyncInit) {
            this.asyncInit = asyncInit;
        }

        public int getMaxWaitThreadCount() {
            return maxWaitThreadCount;
        }

        public void setMaxWaitThreadCount(int maxWaitThreadCount) {
            this.maxWaitThreadCount = maxWaitThreadCount;
        }

        public boolean getUseGlobalDataSourceStat() {
            return useGlobalDataSourceStat;
        }

        public void setUseGlobalDataSourceStat(boolean useGlobalDataSourceStat) {
            this.useGlobalDataSourceStat = useGlobalDataSourceStat;
        }

        public boolean getCreateDataSourceOnStartup() {
            return createDataSourceOnStartup;
        }

        public void setCreateDataSourceOnStartup(boolean createDataSourceOnStartup) {
            this.createDataSourceOnStartup = createDataSourceOnStartup;
        }

        public String getStatViewServlet() {
            return statViewServlet;
        }

        public void setStatViewServlet(String statViewServlet) {
            this.statViewServlet = statViewServlet;
        }

        public String getLoginUsername() {
            return loginUsername;
        }

        public void setLoginUsername(String loginUsername) {
            this.loginUsername = loginUsername;
        }

        public String getLoginPassword() {
            return loginPassword;
        }

        public void setLoginPassword(String loginPassword) {
            this.loginPassword = loginPassword;
        }

        public boolean isUseLocalSessionState() {
            return useLocalSessionState;
        }

        public void setUseLocalSessionState(boolean useLocalSessionState) {
            this.useLocalSessionState = useLocalSessionState;
        }

        public int getInitialSize() {
            return initialSize;
        }

        public void setInitialSize(int initialSize) {
            this.initialSize = initialSize;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(int maxWait) {
            this.maxWait = maxWait;
        }

        public int getTimeBetweenEvictionRunsMillis() {
            return timeBetweenEvictionRunsMillis;
        }

        public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
            this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        }

        public int getMinEvictableIdleTimeMillis() {
            return minEvictableIdleTimeMillis;
        }

        public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
            this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
        }

        public int getValidationQueryTimeout() {
            return validationQueryTimeout;
        }

        public void setValidationQueryTimeout(int validationQueryTimeout) {
            this.validationQueryTimeout = validationQueryTimeout;
        }

        public boolean getTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

        public boolean getTestWhileIdle() {
            return testWhileIdle;
        }

        public void setTestWhileIdle(boolean testWhileIdle) {
            this.testWhileIdle = testWhileIdle;
        }

        public boolean getTestOnReturn() {
            return testOnReturn;
        }

        public void setTestOnReturn(boolean testOnReturn) {
            this.testOnReturn = testOnReturn;
        }

        public boolean getPoolPreparedStatements() {
            return poolPreparedStatements;
        }

        public void setPoolPreparedStatements(boolean poolPreparedStatements) {
            this.poolPreparedStatements = poolPreparedStatements;
        }

        public int getMaxOpenPreparedStatements() {
            return maxOpenPreparedStatements;
        }

        public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
            this.maxOpenPreparedStatements = maxOpenPreparedStatements;
        }

        public boolean getLogAbandoned() {
            return logAbandoned;
        }

        public void setLogAbandoned(boolean logAbandoned) {
            this.logAbandoned = logAbandoned;
        }

        public boolean getRemoveAbandoned() {
            return removeAbandoned;
        }

        public void setRemoveAbandoned(boolean removeAbandoned) {
            this.removeAbandoned = removeAbandoned;
        }

        public int getRemoveAbandonedTimeout() {
            return removeAbandonedTimeout;
        }

        public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
            this.removeAbandonedTimeout = removeAbandonedTimeout;
        }

        public String getFilters() {
            return filters;
        }

        public void setFilters(String filters) {
            this.filters = filters;
        }
    }

    public static class HikariCPProperties implements Serializable{

        /**
         * 为连接池指定一个唯一名称，用于区分多个连接池实例。
         */
        private String poolName;

        /**
         * 连接池中最大连接数
         */
        private int maxPoolSize = 10;

        /**
         * 连接在请求时等待最长的时间(毫秒)
         */
        private long connectionTimeout = 30000;

        /**
         * 连接验证的最长时间（毫秒）
         */
        private long validationTimeout = 5000;

        /**
         * 连接是否应开启自动提交模式。
         * 默认值为 true，表示每个数据库操作都会自动提交。
         */
        private boolean isAutoCommit = true;

        /**
         * 连接是否应处于只读模式。
         * 默认值为 false，表示连接可以进行写操作。
         */
        private boolean isReadOnly = false;

        //连接在池中空闲的最长时间（毫秒）超过此时间的空闲连接将被回收
        private long idleTimeout = 600000;

        //连接泄漏检测的阈值时间（毫秒）如果连接在超过此时间后未被关闭，将记录警告
        private long leakDetectionThreshold = 0;

        //连接的最长生命周期（毫秒）超过此时间的连接将被关闭和移除
        private long maxLifetime = 1800000;

        //连接池中最小空闲连接数
        private int minIdle = 10;

        //池初始化失败的超时时间（毫秒）负值表示忽略初始化失败
        private long initializationFailTimeout = 1;

        //连接初始化时执行的SQL语句
        private String connectionInitSql;

        //自定义的连接测试查询语句
        private String connectionTestQuery;

        //保持连接活跃的时间间隔（毫秒）
        private long keepaliveTime = 0;

        /**
         * 连接池是否应隔离内部查询与用户提供的查询。
         * 默认值为 false，表示不隔离内部查询。
         */
        private boolean isIsolateInternalQueries = false;

        /**
         * 连接池是否应注册JMX MBeans，以通过JMX监控连接池的性能和状态。
         * 默认值为 false，表示不注册MBeans。
         */
        private boolean isRegisterMbeans = false;

        /**
         * 连接池是否允许挂起。
         * 默认值为 false，表示不允许挂起。如果需要在运行时暂停连接池，可以设置为 true。
         */
        private boolean isAllowPoolSuspension = false;

        /**
         * 为基础数据库驱动设置额外的属性。
         */
        private Properties dataSourceProperties = new Properties();

        public String getPoolName() {
            return poolName;
        }

        public void setPoolName(String poolName) {
            this.poolName = poolName;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public long getValidationTimeout() {
            return validationTimeout;
        }

        public void setValidationTimeout(long validationTimeout) {
            this.validationTimeout = validationTimeout;
        }

        public boolean getAutoCommit() {
            return isAutoCommit;
        }

        public void setAutoCommit(boolean autoCommit) {
            isAutoCommit = autoCommit;
        }

        public boolean getReadOnly() {
            return isReadOnly;
        }

        public void setReadOnly(boolean readOnly) {
            isReadOnly = readOnly;
        }

        public boolean getIsolateInternalQueries() {
            return isIsolateInternalQueries;
        }

        public boolean getRegisterMbeans() {
            return isRegisterMbeans;
        }

        public boolean getAllowPoolSuspension() {
            return isAllowPoolSuspension;
        }

        public Properties getDataSourceProperties() {
            return dataSourceProperties;
        }

        public void setDataSourceProperties(Properties dataSourceProperties) {
            this.dataSourceProperties = dataSourceProperties;
        }

        public boolean getIsIsolateInternalQueries() {
            return isIsolateInternalQueries;
        }

        public void setIsolateInternalQueries(boolean isolateInternalQueries) {
            isIsolateInternalQueries = isolateInternalQueries;
        }

        public boolean getIsRegisterMbeans() {
            return isRegisterMbeans;
        }

        public void setRegisterMbeans(boolean registerMbeans) {
            isRegisterMbeans = registerMbeans;
        }

        public boolean getIsAllowPoolSuspension() {
            return isAllowPoolSuspension;
        }

        public void setAllowPoolSuspension(boolean allowPoolSuspension) {
            isAllowPoolSuspension = allowPoolSuspension;
        }

        public long getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public long getLeakDetectionThreshold() {
            return leakDetectionThreshold;
        }

        public void setLeakDetectionThreshold(long leakDetectionThreshold) {
            this.leakDetectionThreshold = leakDetectionThreshold;
        }

        public long getMaxLifetime() {
            return maxLifetime;
        }

        public void setMaxLifetime(long maxLifetime) {
            this.maxLifetime = maxLifetime;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public long getInitializationFailTimeout() {
            return initializationFailTimeout;
        }

        public void setInitializationFailTimeout(long initializationFailTimeout) {
            this.initializationFailTimeout = initializationFailTimeout;
        }

        public String getConnectionInitSql() {
            return connectionInitSql;
        }

        public void setConnectionInitSql(String connectionInitSql) {
            this.connectionInitSql = connectionInitSql;
        }

        public String getConnectionTestQuery() {
            return connectionTestQuery;
        }

        public void setConnectionTestQuery(String connectionTestQuery) {
            this.connectionTestQuery = connectionTestQuery;
        }

        public long getKeepaliveTime() {
            return keepaliveTime;
        }

        public void setKeepaliveTime(long keepaliveTime) {
            this.keepaliveTime = keepaliveTime;
        }
    }

    public ConnectionPoolTypeEnum getConnectionPoolType() {
        return connectionPoolType;
    }

    public void setConnectionPoolType(ConnectionPoolTypeEnum connectionPoolType) {
        this.connectionPoolType = connectionPoolType;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public HikariCPProperties getHikariCP() {
        return hikariCP;
    }

    public void setHikariCP(HikariCPProperties hikariCP) {
        this.hikariCP = hikariCP;
    }

    public DruidProperties getDruidProperties() {
        return druidProperties;
    }

    public void setDruidProperties(DruidProperties druidProperties) {
        this.druidProperties = druidProperties;
    }
}
