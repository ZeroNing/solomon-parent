package com.steven.solomon.mqtt.v3.profile;

import java.io.Serializable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * MQTT3 客户端连接配置模型。
 *
 * <p>映射 {@code mqtt.tenant.*} 下的单租户配置，包括认证、超时、遗嘱消息和 SSL 等参数。</p>
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

  /** 客户端掉线后是否自动清除 session */
  private boolean cleanSession = false;

  /** 心跳时间（秒） */
  @Min(value = 1, message = "mqtt.tenant[].keep-alive-interval must be at least 1 second")
  private int keepAliveInterval = 60;

  /** 遗嘱消息配置 */
  @Valid
  private MqttWill will;

  /** 最大未确认消息数量 */
  @Min(value = 1, message = "mqtt.tenant[].max-inflight must be at least 1")
  private int maxInflight = 10;

  /** 重新连接之间等待的最长时间（毫秒） */
  @Min(value = 1, message = "mqtt.tenant[].max-reconnect-delay must be at least 1 millisecond")
  private int maxReconnectDelay = 12800;

  /** 连接超时值（秒），0 表示禁用超时 */
  @Min(value = 0, message = "mqtt.tenant[].connection-timeout must not be negative")
  private int connectionTimeout = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;

  /** 执行器服务终止前等待的时间（秒） */
  @Min(value = 1, message = "mqtt.tenant[].executor-service-timeout must be at least 1 second")
  private int executorServiceTimeout = 1;

  /** SSL 连接是否验证证书 */
  private boolean verifyCertificate = false;

  /**
   * MQTT 协议版本。
   *
   * <p>取值参考 {@link org.eclipse.paho.client.mqttv3.MqttConnectOptions#MQTT_VERSION_3_1}、
   * {@link org.eclipse.paho.client.mqttv3.MqttConnectOptions#MQTT_VERSION_3_1_1}。
   * 默认值 0 表示由客户端自动选择。</p>
   */
  @Min(value = 0, message = "mqtt.tenant[].mqtt-version must be 0, 3, or 4")
  @Max(value = 4, message = "mqtt.tenant[].mqtt-version must be 0, 3, or 4")
  private int mqttVersion = 0;

  /**
   * MQTT 遗嘱消息配置。
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

  public boolean getCleanSession() {
    return cleanSession;
  }

  public void setCleanSession(boolean cleanSession) {
    this.cleanSession = cleanSession;
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

  public int getMaxInflight() {
    return maxInflight;
  }

  public void setMaxInflight(int maxInflight) {
    this.maxInflight = maxInflight;
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

  public boolean getVerifyCertificate() {
    return verifyCertificate;
  }

  public void setVerifyCertificate(boolean verifyCertificate) {
    this.verifyCertificate = verifyCertificate;
  }

  public int getMqttVersion() {
    return mqttVersion;
  }

  public void setMqttVersion(int mqttVersion) {
    this.mqttVersion = mqttVersion;
  }
}
