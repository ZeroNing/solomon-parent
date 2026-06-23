package com.steven.solomon.mqtt.v5.profile;

import java.io.Serializable;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * MQTT5 客户端连接配置模型。
 *
 * <p>映射 {@code mqtt.tenant.*} 下的单租户配置，包括认证、超时、遗嘱消息、MQTT5 专有属性和 SSL 等参数。</p>
 */
public class MqttProfile {

  /** 用户名 */
  private String userName;

  /** 密码 */
  private String password;

  /** MQTT Broker 连接地址，多个地址以逗号分隔 */
  @NotBlank(message = "mqtt.tenant[].url must not be blank")
  private String url;

  /** 客户端的标识（不可重复，为空时使用 UUID） */
  @NotBlank(message = "mqtt.tenant[].client-id must not be blank")
  private String clientId;

  /** 连接超时（秒） */
  @Min(value = 1, message = "mqtt.tenant[].completion-timeout must be at least 1 second")
  private int completionTimeout = 30;

  /** 是否自动重连 */
  private boolean automaticReconnect = true;

  /** 客户端是否应该在连接时重置会话状态 */
  private boolean cleanStart = false;

  /** 心跳时间（秒） */
  @Min(value = 1, message = "mqtt.tenant[].keep-alive-interval must be at least 1 second")
  private int keepAliveInterval = 60;

  /** 遗嘱消息配置 */
  @Valid
  private MqttWill will;

  /** 客户端愿意接收的 QoS 1 和 QoS 2 消息的最大数量 */
  @Min(value = 1, message = "mqtt.tenant[].receive-maximum must be at least 1")
  @Max(value = 65535, message = "mqtt.tenant[].receive-maximum must be at most 65535")
  private int receiveMaximum = 65535;

  /** 重新连接之间等待的最长时间（毫秒） */
  @Min(value = 1, message = "mqtt.tenant[].max-reconnect-delay must be at least 1 millisecond")
  private int maxReconnectDelay = 12800;

  /** 连接超时值（秒），0 表示禁用超时 */
  @Min(value = 0, message = "mqtt.tenant[].connection-timeout must not be negative")
  private int connectionTimeout = 30;

  /** 执行器服务终止前等待的时间（秒） */
  @Min(value = 1, message = "mqtt.tenant[].executor-service-timeout must be at least 1 second")
  private int executorServiceTimeout = 1;

  /** 是否请求服务器在发生错误时发送问题信息 */
  private Boolean requestProblemInformation = false;

  /** 是否请求服务器发送响应信息 */
  private Boolean requestResponseInformation = false;

  /** 客户端愿意接收的最大 MQTT 数据包大小 */
  @Positive(message = "mqtt.tenant[].maximum-packet-size must be positive")
  private Long maximumPacketSize;

  /** 自动重新连接尝试之间的最小延迟时间（秒） */
  @Min(value = 1, message = "mqtt.tenant[].automatic-reconnect-min-delay must be at least 1 second")
  private int automaticReconnectMinDelay = 1;

  /** 自动重新连接尝试之间的最大延迟时间（秒） */
  @Min(value = 1, message = "mqtt.tenant[].automatic-reconnect-max-delay must be at least 1 second")
  private int automaticReconnectMaxDelay = 120;

  /** 是否发送原因码消息 */
  private Boolean sendReasonMessages = false;

  /** 会话过期间隔时间（秒），null 表示无限期 */
  @Min(value = 0, message = "mqtt.tenant[].session-expiry-interval must not be negative")
  private Long sessionExpiryInterval = null;

  /** SSL 连接是否验证证书 */
  private boolean verifyCertificate = false;

  /**
   * 是否支持主题别名。
   *
   * <p>值为 0 表示不使用主题别名功能，正值表示客户端愿意使用的最大主题别名数量。</p>
   */
  @Min(value = 0, message = "mqtt.tenant[].topic-alias-max must not be negative")
  private Integer topicAliasMax = 0;

  /**
   * 用户属性集合。
   *
   * <p>MQTT 5.0 CONNECT 报文可携带键值对形式的用户属性，用于传递自定义元数据。</p>
   */
  private Map<@NotBlank(message = "mqtt.tenant[].user-properties key must not be blank") String,
      @NotBlank(message = "mqtt.tenant[].user-properties value must not be blank") String> userProperties;

  /**
   * MQTT5 遗嘱消息配置。
   */
  public static class MqttWill implements Serializable {

    /** 遗嘱主题 */
    @NotBlank(message = "mqtt.tenant[].will.topic must not be blank")
    private String topic;

    /** 遗嘱消息内容 */
    @NotBlank(message = "mqtt.tenant[].will.message must not be blank")
    private String message;

    /** 遗嘱消息 QoS 等级 */
    @Min(value = 0, message = "mqtt.tenant[].will.qos must be between 0 and 2")
    @Max(value = 2, message = "mqtt.tenant[].will.qos must be between 0 and 2")
    private int qos;

    /** 是否保留消息 */
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

  public boolean getVerifyCertificate() {
    return verifyCertificate;
  }

  public void setVerifyCertificate(boolean verifyCertificate) {
    this.verifyCertificate = verifyCertificate;
  }

  public Integer getTopicAliasMax() {
    return topicAliasMax;
  }

  public void setTopicAliasMax(Integer topicAliasMax) {
    this.topicAliasMax = topicAliasMax;
  }

  public Map<String, String> getUserProperties() {
    return userProperties;
  }

  public void setUserProperties(Map<String, String> userProperties) {
    this.userProperties = userProperties;
  }
}
