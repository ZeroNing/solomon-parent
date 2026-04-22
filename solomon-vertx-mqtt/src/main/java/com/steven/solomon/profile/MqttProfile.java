package com.steven.solomon.profile;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.mqtt.MqttClientOptions;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class MqttProfile {

  /**
   * 用户名
   */
  private String userName;

  /**
   * 密码
   */
  private String password;

  /**
   * 连接
   */
  private String url;

  /**
   * 客户端的标识(不可重复,为空时侯用uuid)
   */
  private String clientId;

  /**
   * 连接超时（毫秒）
   */
  private int completionTimeout = 30000;

  /**
   * 是否自动重连
   */
  private boolean automaticReconnect = true;

  /**
   * 客户端掉线后,是否自动清除session
   */
  private boolean cleanSession = false;

  /**
   * 心跳时间
   */
  private int keepAliveInterval = MqttClientOptions.DEFAULT_KEEP_ALIVE_INTERVAL;
  /**
   * 遗嘱消息
   */
  private MqttWill will;
  /**
   * 最大未确认消息数量
   */
  private int maxInflight = MqttClientOptions.DEFAULT_MAX_INFLIGHT_QUEUE;

  /**
   * 重连次数(-1 无限重连 0 不重连)
   */
  private int reconnectAttempts = -1;

  /**
   * 重连间隔（毫秒）- 注意：Vert.x 5.x 不支持自动重连
   */
  private long reconnectInterval = 1000;


  /**
   * ssl连接是否验证证书
   */
  private boolean verifyCertificate = false;

  private VertxConfig vertx;

  public static class MqttWill implements Serializable {

    /**
     * 遗嘱主题
     */
    private String topic;
    /**
     * 遗嘱消息
     */
    private String message;
    /**
     * 遗嘱消息质量
     */
    private int qos;

    /**
     * 是否保留消息
     */
    private boolean retained;

    public boolean getRetained() {
      return retained;
    }

    public void setRetained(boolean retained) {
      this.retained = retained;
    }

    public String getTopic() {
      return topic;
    }

    public void setTopic(String topic) {
      this.topic = topic;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public int getQos() {
      return qos;
    }

    public void setQos(int qos) {
      this.qos = qos;
    }
  }

  public static class VertxConfig implements Serializable {

    // ==================== 基本配置 ====================

    /**
     * 事件循环线程池大小。默认值：2 * CPU 核心数。
     */
    private int eventLoopPoolSize = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;

    /**
     * Worker 线程池大小。默认值：20。
     */
    private int workerPoolSize = VertxOptions.DEFAULT_WORKER_POOL_SIZE;

    /**
     * 内部阻塞线程池大小。默认值：20。
     */
    private int internalBlockingPoolSize = VertxOptions.DEFAULT_INTERNAL_BLOCKING_POOL_SIZE;

    /**
     * 阻塞线程检查间隔（数值）。默认值：1（秒）。
     */
    private long blockedThreadCheckInterval = 1L;
    /**
     * 阻塞线程检查间隔的时间单位。默认值：SECONDS。
     */
    private TimeUnit blockedThreadCheckIntervalUnit = TimeUnit.SECONDS;

    /**
     * 事件循环线程最大执行时间（数值）。默认值：2（秒）。
     */
    private long maxEventLoopExecuteTime = 2L;
    /**
     * 事件循环线程最大执行时间单位。默认值：SECONDS。
     */
    private TimeUnit maxEventLoopExecuteTimeUnit = TimeUnit.SECONDS;

    /**
     * Worker 线程最大执行时间（数值）。默认值：60（秒）。
     */
    private long maxWorkerExecuteTime = 60L;
    /**
     * Worker 线程最大执行时间单位。默认值：SECONDS。
     */
    private TimeUnit maxWorkerExecuteTimeUnit = TimeUnit.SECONDS;

    /**
     * 警告异常时间（数值）。默认值：5（秒）。
     */
    private long warningExceptionTime = 5L;
    /**
     * 警告异常时间单位。默认值：SECONDS。
     */
    private TimeUnit warningExceptionTimeUnit = TimeUnit.SECONDS;

    // ==================== 高可用配置 ====================

    /**
     * 是否启用高可用模式。默认值：false。
     */
    private boolean haEnabled = false;

    /**
     * 法定节点数。默认值：1。
     */
    private int quorumSize = 1;

    /**
     * 高可用组名。默认值：__DEFAULT__。
     */
    private String haGroup = "__DEFAULT__";

    // ==================== 高级配置 ====================

    /**
     * 是否优先使用原生传输（如 epoll、kqueue）。默认值：false。
     */
    private boolean preferNativeTransport = false;

    /**
     * 是否禁用线程上下文类加载器（TCCL）。默认值：false。
     */
    private boolean disableTCCL = false;

    /**
     * 是否使用守护线程。默认值：false。
     */
    private Boolean useDaemonThread = false;

    public int getEventLoopPoolSize() {
      return eventLoopPoolSize;
    }

    public void setEventLoopPoolSize(int eventLoopPoolSize) {
      this.eventLoopPoolSize = eventLoopPoolSize;
    }

    public int getWorkerPoolSize() {
      return workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
      this.workerPoolSize = workerPoolSize;
    }

    public int getInternalBlockingPoolSize() {
      return internalBlockingPoolSize;
    }

    public void setInternalBlockingPoolSize(int internalBlockingPoolSize) {
      this.internalBlockingPoolSize = internalBlockingPoolSize;
    }

    public long getBlockedThreadCheckInterval() {
      return blockedThreadCheckInterval;
    }

    public void setBlockedThreadCheckInterval(long blockedThreadCheckInterval) {
      this.blockedThreadCheckInterval = blockedThreadCheckInterval;
    }

    public TimeUnit getBlockedThreadCheckIntervalUnit() {
      return blockedThreadCheckIntervalUnit;
    }

    public void setBlockedThreadCheckIntervalUnit(TimeUnit blockedThreadCheckIntervalUnit) {
      this.blockedThreadCheckIntervalUnit = blockedThreadCheckIntervalUnit;
    }

    public long getMaxEventLoopExecuteTime() {
      return maxEventLoopExecuteTime;
    }

    public void setMaxEventLoopExecuteTime(long maxEventLoopExecuteTime) {
      this.maxEventLoopExecuteTime = maxEventLoopExecuteTime;
    }

    public TimeUnit getMaxEventLoopExecuteTimeUnit() {
      return maxEventLoopExecuteTimeUnit;
    }

    public void setMaxEventLoopExecuteTimeUnit(TimeUnit maxEventLoopExecuteTimeUnit) {
      this.maxEventLoopExecuteTimeUnit = maxEventLoopExecuteTimeUnit;
    }

    public long getMaxWorkerExecuteTime() {
      return maxWorkerExecuteTime;
    }

    public void setMaxWorkerExecuteTime(long maxWorkerExecuteTime) {
      this.maxWorkerExecuteTime = maxWorkerExecuteTime;
    }

    public TimeUnit getMaxWorkerExecuteTimeUnit() {
      return maxWorkerExecuteTimeUnit;
    }

    public void setMaxWorkerExecuteTimeUnit(TimeUnit maxWorkerExecuteTimeUnit) {
      this.maxWorkerExecuteTimeUnit = maxWorkerExecuteTimeUnit;
    }

    public long getWarningExceptionTime() {
      return warningExceptionTime;
    }

    public void setWarningExceptionTime(long warningExceptionTime) {
      this.warningExceptionTime = warningExceptionTime;
    }

    public TimeUnit getWarningExceptionTimeUnit() {
      return warningExceptionTimeUnit;
    }

    public void setWarningExceptionTimeUnit(TimeUnit warningExceptionTimeUnit) {
      this.warningExceptionTimeUnit = warningExceptionTimeUnit;
    }

    public boolean isHaEnabled() {
      return haEnabled;
    }

    public void setHaEnabled(boolean haEnabled) {
      this.haEnabled = haEnabled;
    }

    public int getQuorumSize() {
      return quorumSize;
    }

    public void setQuorumSize(int quorumSize) {
      this.quorumSize = quorumSize;
    }

    public String getHaGroup() {
      return haGroup;
    }

    public void setHaGroup(String haGroup) {
      this.haGroup = haGroup;
    }

    public boolean isPreferNativeTransport() {
      return preferNativeTransport;
    }

    public void setPreferNativeTransport(boolean preferNativeTransport) {
      this.preferNativeTransport = preferNativeTransport;
    }

    public boolean isDisableTCCL() {
      return disableTCCL;
    }

    public void setDisableTCCL(boolean disableTCCL) {
      this.disableTCCL = disableTCCL;
    }

    public Boolean getUseDaemonThread() {
      return useDaemonThread;
    }

    public void setUseDaemonThread(Boolean useDaemonThread) {
      this.useDaemonThread = useDaemonThread;
    }
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

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public int getCompletionTimeout() {
    return completionTimeout;
  }

  public void setCompletionTimeout(int completionTimeout) {
    this.completionTimeout = completionTimeout;
  }

  public boolean isAutomaticReconnect() {
    return automaticReconnect;
  }

  public void setAutomaticReconnect(boolean automaticReconnect) {
    this.automaticReconnect = automaticReconnect;
  }

  public boolean isCleanSession() {
    return cleanSession;
  }

  public void setCleanSession(boolean cleanSession) {
    this.cleanSession = cleanSession;
  }

  public int getKeepAliveInterval() {
    return keepAliveInterval;
  }

  public void setKeepAliveInterval(int keepAliveInterval) {
    this.keepAliveInterval = keepAliveInterval;
  }

  public MqttWill getWill() {
    return will;
  }

  public void setWill(MqttWill will) {
    this.will = will;
  }

  public int getMaxInflight() {
    return maxInflight;
  }

  public void setMaxInflight(int maxInflight) {
    this.maxInflight = maxInflight;
  }

  public int getReconnectAttempts() {
    return reconnectAttempts;
  }

  public void setReconnectAttempts(int reconnectAttempts) {
    this.reconnectAttempts = reconnectAttempts;
  }

  public long getReconnectInterval() {
    return reconnectInterval;
  }

  public void setReconnectInterval(long reconnectInterval) {
    this.reconnectInterval = reconnectInterval;
  }

  public boolean isVerifyCertificate() {
    return verifyCertificate;
  }

  public void setVerifyCertificate(boolean verifyCertificate) {
    this.verifyCertificate = verifyCertificate;
  }

  public VertxConfig getVertx() {
    return vertx;
  }

  public void setVertx(VertxConfig vertx) {
    this.vertx = vertx;
  }
}
