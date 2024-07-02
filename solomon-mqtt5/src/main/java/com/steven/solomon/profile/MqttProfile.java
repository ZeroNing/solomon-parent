package com.steven.solomon.profile;

import java.io.Serializable;

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
   * 连接超时
   */
  private int completionTimeout = 30;

  /**
   * 是否自动重连
   */
  private boolean automaticReconnect = true;

  /**
   * 客户端是否应该在连接时重置会话状态。如果设置为 true，客户端将不会恢复之前的会话状态，而是开始一个新的会话。如果设置为 false，客户端将尝试恢复之前的会话状态。
   */
  private boolean cleanStart = false;

  /**
   * 心跳时间
   */
  private int keepAliveInterval = 60;
  /**
   * 遗嘱消息
   */
  private MqttWill will;
  /**
   * 客户端愿意接收的 QoS 1 和 QoS 2 消息的最大数量
   */
  private int receiveMaximum = 65535;

  /**
   * 重新连接之间等待的最长时间
   */
  private int maxReconnectDelay = 12800;

  /**
   * 设置连接超时值,该值以秒为单位 0 禁用超时处理,这意味着客户端将等待，直到网络连接成功或失败.
   */
  private int connectionTimeout = 30;

  /**
   * 设置执行器服务应等待的时间（以秒为单位）在强制终止之前终止。不建议更改除非您绝对确定需要，否则该值。
   */
  private int executorServiceTimeout = 1;

  /**
   * 指示客户端是否请求服务器在发生错误时发送问题信息
   */
  private Boolean requestProblemInformation = false;

  /**
   * 指示客户端是否请求服务器发送响应信息
   */
  private Boolean requestResponseInformation = false;

  /**
   * 客户端愿意接收的最大 MQTT 数据包大小
   */
  private Long maximumPacketSize;

  /**
   * 自动重新连接尝试之间的最小延迟时间
   */
  private int automaticReconnectMinDelay = 1;

  /**
   * 自动重新连接尝试之间的最大延迟时间
   */
  private int automaticReconnectMaxDelay = 120;

  /**
   * 指示是否发送原因码消息
   */
  private Boolean sendReasonMessages = false;

  /**
   * 会话过期间隔时间（以秒为单位），如果设置为 null，则使用默认值（通常是无限期）
   */
  private Long sessionExpiryInterval = null;

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

  public MqttWill getWill() {
    return will;
  }

  public void setWill(MqttWill will) {
    this.will = will;
  }

  public int getKeepAliveInterval() {
    return keepAliveInterval;
  }

  public void setKeepAliveInterval(int keepAliveInterval) {
    this.keepAliveInterval = keepAliveInterval;
  }

  public boolean getCleanStart() {
    return cleanStart;
  }

  public void setCleanStart(boolean cleanStart) {
    this.cleanStart = cleanStart;
  }

  public boolean getAutomaticReconnect() {
    return automaticReconnect;
  }

  public void setAutomaticReconnect(boolean automaticReconnect) {
    this.automaticReconnect = automaticReconnect;
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

  public int getReceiveMaximum() {
    return receiveMaximum;
  }

  public void setReceiveMaximum(int receiveMaximum) {
    this.receiveMaximum = receiveMaximum;
  }

  public int getMaxReconnectDelay() {
    return maxReconnectDelay;
  }

  public void setMaxReconnectDelay(int maxReconnectDelay) {
    this.maxReconnectDelay = maxReconnectDelay;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public int getExecutorServiceTimeout() {
    return executorServiceTimeout;
  }

  public void setExecutorServiceTimeout(int executorServiceTimeout) {
    this.executorServiceTimeout = executorServiceTimeout;
  }

  public Boolean getRequestProblemInformation() {
    return requestProblemInformation;
  }

  public void setRequestProblemInformation(Boolean requestProblemInformation) {
    this.requestProblemInformation = requestProblemInformation;
  }

  public Boolean getRequestResponseInformation() {
    return requestResponseInformation;
  }

  public void setRequestResponseInformation(Boolean requestResponseInformation) {
    this.requestResponseInformation = requestResponseInformation;
  }

  public Long getMaximumPacketSize() {
    return maximumPacketSize;
  }

  public void setMaximumPacketSize(Long maximumPacketSize) {
    this.maximumPacketSize = maximumPacketSize;
  }

  public int getAutomaticReconnectMinDelay() {
    return automaticReconnectMinDelay;
  }

  public void setAutomaticReconnectMinDelay(int automaticReconnectMinDelay) {
    this.automaticReconnectMinDelay = automaticReconnectMinDelay;
  }

  public int getAutomaticReconnectMaxDelay() {
    return automaticReconnectMaxDelay;
  }

  public void setAutomaticReconnectMaxDelay(int automaticReconnectMaxDelay) {
    this.automaticReconnectMaxDelay = automaticReconnectMaxDelay;
  }

  public Boolean getSendReasonMessages() {
    return sendReasonMessages;
  }

  public void setSendReasonMessages(Boolean sendReasonMessages) {
    this.sendReasonMessages = sendReasonMessages;
  }

  public Long getSessionExpiryInterval() {
    return sessionExpiryInterval;
  }

  public void setSessionExpiryInterval(Long sessionExpiryInterval) {
    this.sessionExpiryInterval = sessionExpiryInterval;
  }
}
