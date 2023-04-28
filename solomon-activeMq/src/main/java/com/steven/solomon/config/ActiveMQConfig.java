package com.steven.solomon.config;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"springUtil"})
public class ActiveMQConfig {

  @Bean
  public Connection connection(ConnectionFactory connectionFactory) throws JMSException {
    Connection connection = connectionFactory.createConnection();
    connection.start();
    return connection;
  }

  @Bean
  public JmsPoolConnectionFactoryFactory factory(ActiveMQProperties properties){
    return new JmsPoolConnectionFactoryFactory(properties.getPool());
  }


}
