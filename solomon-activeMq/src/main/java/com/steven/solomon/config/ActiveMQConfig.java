package com.steven.solomon.config;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.steven.solomon.profile.ActiveMQProfile;
import com.steven.solomon.verification.ValidateUtils;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"springUtil"})
public class ActiveMQConfig {

  private final ActiveMQProfile profile;

  public ActiveMQConfig(ActiveMQProfile profile) {this.profile = profile;}

  @Bean
  public ConnectionFactory connectionFactory(){
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(profile.getUserName(),profile.getPassword(),profile.getUrl());
    connectionFactory.setClientID(ValidateUtils.isEmpty(profile.getClientId()) ? UUID.randomUUID().toString() : profile.getClientId());
    return connectionFactory;
  }

  @Bean
  public Connection connection(ConnectionFactory connectionFactory) throws JMSException {
    Connection connection = connectionFactory.createConnection();
    connection.start();
    return connection;
  }

  @Bean
  public JmsPoolConnectionFactoryFactory factory(){
    return new JmsPoolConnectionFactoryFactory(profile.getPool());
  }


}
