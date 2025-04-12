package com.steven.solomon.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("xxl")
public class XxlJobProperties {

    // XXL-Job 管理控制台的地址。
    private String adminAddresses;

    // 用于与管理控制台安全通信的访问令牌。
    private String accessToken;

    // 执行器的应用名称，用于注册和标识。
    private String appName;

    // 执行器的自定义地址（如果有指定）。
    private String address;

    // 执行器机器的 IP 地址。
    private String ip;

    // 执行器运行的端口号。
    private int port;

    // 存储执行日志的路径。
    private String logPath;

    // 保留执行日志的天数。
    private Integer logRetentionDays = 30;

    //网页登陆账户
    private String userName;

    //网页登陆密码
    private String password;

    //是否启用
    private boolean enabled = true;

    //超时时间
    private Integer timeout;

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
