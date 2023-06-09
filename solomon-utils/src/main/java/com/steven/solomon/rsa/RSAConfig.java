package com.steven.solomon.rsa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RSAConfig {

  @Value("${public-key:swagger文档}")
  private String publicKey;

  @Value("${private-key:swagger文档}")
  private String privateKey;


  @Bean
  public RSAUtils init() {
    return new RSAUtils(publicKey, privateKey);
  }
}
