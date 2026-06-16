package com.steven.solomon.job.xxl.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** XXL-JOB Executor 启动配置。 */
@ConfigurationProperties("xxl")
public class XxlJobProperties {

    /** 是否启用 XXL-JOB Executor，默认启用。 */
    private boolean enabled = true;

    /** XXL-JOB 管理端地址，多个用逗号分隔。 */
    private String adminAddresses;

    /** 与 XXL-JOB 管理端通信的访问令牌。 */
    private String accessToken;

    /** 执行器应用名称（AppName），用于注册到调度中心识别。 */
    private String appName;

    /** 执行器注册地址，为空时自动获取。 */
    private String address;

    /** 执行器注册 IP。 */
    private String ip;

    /** 执行器注册端口。 */
    private int port;

    /** 执行器日志路径。 */
    private String logPath;

    /** 日志保留天数，默认 30 天。 */
    private Integer logRetentionDays = 30;

    /** 任务执行超时时间，单位秒。 */
    private Integer timeout;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAdminAddresses() {
        return adminAddresses;
    }

    public void setAdminAddresses(String adminAddresses) {
        this.adminAddresses = adminAddresses;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public int getLogRetentionDays() {
        return logRetentionDays;
    }

    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
