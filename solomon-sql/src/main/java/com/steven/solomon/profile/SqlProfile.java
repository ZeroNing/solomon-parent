package com.steven.solomon.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@ConfigurationProperties("spring.data.sql")
public class SqlProfile {

    private HikariData hikariData;

    public HikariData getHikariData() {
        return hikariData;
    }

    public void setHikariData(HikariData hikariData) {
        this.hikariData = hikariData;
    }

    public static class HikariData implements Serializable{

        //数据库地址
        private String url;

        //数据库账号
        private String username;

        //数据库密码
        private String password;

        //数据库驱动类的完全限定名
        private String driverClassName;

        //连接在请求时等待最长的时间(毫秒)
        private long connectionTimeout = 30000;

        //连接验证的最长时间（毫秒）
        private long validationTimeout = 5000;

        //连接在池中空闲的最长时间（毫秒）超过此时间的空闲连接将被回收
        private long idleTimeout = 600000;

        //连接泄漏检测的阈值时间（毫秒）如果连接在超过此时间后未被关闭，将记录警告
        private long leakDetectionThreshold = 0;

        //连接的最长生命周期（毫秒）超过此时间的连接将被关闭和移除
        private long maxLifetime = 1800000;

        //连接池中最大连接数
        private int maxPoolSize = 10;

        //连接池中最小空闲连接数
        private int minIdle = maxPoolSize;

        //池初始化失败的超时时间（毫秒）负值表示忽略初始化失败
        private long initializationFailTimeout = 1;

        //连接初始化时执行的SQL语句
        private String connectionInitSql;

        //自定义的连接测试查询语句
        private String connectionTestQuery;

        //保持连接活跃的时间间隔（毫秒）
        private long keepaliveTime = 0;

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

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
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
    }
}
