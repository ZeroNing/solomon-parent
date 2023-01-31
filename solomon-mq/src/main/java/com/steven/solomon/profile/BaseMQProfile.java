package com.steven.solomon.profile;

import com.steven.solomon.enums.MqChoiceEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("mq")
@Component
public class BaseMQProfile {
  /**
   * 不启用的队列名，用逗号隔开
   */
  String notEnabledQueue;

  /**
   * 连接地址
   */
  String host;

  /**
   * 端口
   */
  int port;

  /**
   * 用户名
   */
  String userName;

  /**
   * 密码
   */
  String password;

  /**
   * mq选择器
   */
  MqChoiceEnum choice;

  public MqChoiceEnum getChoice() {
    return choice;
  }

  public void setChoice(MqChoiceEnum choice) {
    this.choice = choice;
  }

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

  public String getNotEnabledQueue() {
    return notEnabledQueue;
  }

  public void setNotEnabledQueue(String notEnabledQueue) {
    this.notEnabledQueue = notEnabledQueue;
  }
}
