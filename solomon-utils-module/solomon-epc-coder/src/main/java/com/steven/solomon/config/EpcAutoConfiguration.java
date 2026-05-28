package com.steven.solomon.config;

import com.steven.solomon.service.EpcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EpcAutoConfiguration {

  /**
   * 注册默认 EPC 服务。
   *
   * @return EPC 服务
   */
  @Bean
  @ConditionalOnMissingBean
  public EpcService epcService() {
    return new EpcService();
  }
}
