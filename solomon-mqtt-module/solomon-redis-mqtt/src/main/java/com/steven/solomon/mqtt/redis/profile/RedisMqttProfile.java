package com.steven.solomon.mqtt.redis.profile;

import java.io.Serializable;

/**
 * Redis MQTT 风格实现配置。
 */
public class RedisMqttProfile implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Redis 主机。
   */
  private String host = "localhost";

  /**
   * Redis 端口。
   */
  private int port = 6379;

  /**
   * Redis 数据库索引。
   */
  private int database = 0;

  /**
   * Redis 用户名，Redis 6 ACL 场景使用。
   */
  private String username;

  /**
   * Redis 密码。
   */
  private String password;

  /**
   * 是否启用 Redis SSL。
   */
  private boolean ssl = false;

  /**
   * Redis 命令超时时间，单位毫秒。
   */
  private long timeout = 60000L;

  /**
   * 通道前缀，用于隔离不同业务或环境。
   */
  private String channelPrefix;

  /**
   * 是否把租户编码拼到 Redis 通道前面。
   */
  private boolean useTenantPrefix = true;

  /**
   * 是否把所有订阅都作为 Redis PatternTopic 处理。
   */
  private boolean patternTopic = false;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getDatabase() {
    return database;
  }

  public void setDatabase(int database) {
    this.database = database;
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

  public boolean isSsl() {
    return ssl;
  }

  public void setSsl(boolean ssl) {
    this.ssl = ssl;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public String getChannelPrefix() {
    return channelPrefix;
  }

  public void setChannelPrefix(String channelPrefix) {
    this.channelPrefix = channelPrefix;
  }

  public boolean isUseTenantPrefix() {
    return useTenantPrefix;
  }

  public void setUseTenantPrefix(boolean useTenantPrefix) {
    this.useTenantPrefix = useTenantPrefix;
  }

  public boolean isPatternTopic() {
    return patternTopic;
  }

  public void setPatternTopic(boolean patternTopic) {
    this.patternTopic = patternTopic;
  }
}
