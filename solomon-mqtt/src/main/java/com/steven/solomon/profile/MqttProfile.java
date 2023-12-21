package com.steven.solomon.profile;

import com.steven.solomon.config.MqttConfig;
import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.stereotype.Component;

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
   * 客户端掉线后,是否自动清除session
   */
  private boolean cleanSession = false;

  /**
   * 心跳时间
   */
  private int keepAliveInterval = 60;
  /**
   * 遗嘱消息
   */
  private MqttWill will;
  /**
   * 最大未确认消息数量
   */
  private int maxInflight = 10;

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
}
