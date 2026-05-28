package com.steven.solomon.profile;

import java.io.Serializable;
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
  private String url;

  /** 客户端的标识（不可重复，为空时使用 UUID） */
  private String clientId;

  /** 连接超时（秒） */
  private int completionTimeout = 30;

  /** 是否自动重连 */
  private boolean automaticReconnect = true;

  /** 客户端掉线后是否自动清除 session */
  private boolean cleanSession = false;

  /** 心跳时间（秒） */
  private int keepAliveInterval = 60;

  /** 遗嘱消息配置 */
  private MqttWill will;

  /** 最大未确认消息数量 */
  private int maxInflight = 10;

  /** 重新连接之间等待的最长时间（毫秒） */
  private int maxReconnectDelay = 12800;

  /** 连接超时值（秒），0 表示禁用超时 */
  private int connectionTimeout = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;

  /** 执行器服务终止前等待的时间（秒） */
  private int executorServiceTimeout = 1;

  /** SSL 连接是否验证证书 */
  private boolean verifyCertificate = false;

  /**
   * MQTT 遗嘱消息配置。
   */
  public static class MqttWill implements Serializable {

    /** 遗嘱主题 */
    private String topic;

    /** 遗嘱消息内容 */
    private String message;

    /** 遗嘱消息 QoS 等级 */
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
}
