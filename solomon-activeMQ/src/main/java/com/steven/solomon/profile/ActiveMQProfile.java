package com.steven.solomon.profile;

import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties("active")
@Component
public class ActiveMQProfile {

  /**
   * 链接地址
   */
  private String url;

  /**
   * 用户名
   */
  private String userName;

  /**
   * 密码
   */
  private String password;

  /**
   * ClientId 为空的时候默认为UUID
   */
  private String clientId;

  @NestedConfigurationProperty
  private final JmsPoolConnectionFactoryProperties pool = new JmsPoolConnectionFactoryProperties();

  public JmsPoolConnectionFactoryProperties getPool() {
    return pool;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
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
}
