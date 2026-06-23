package com.steven.solomon.job.xxl.config;

import com.steven.solomon.job.xxl.health.XxlJobAdminHealthIndicator;
import com.steven.solomon.job.xxl.properties.XxlJobRegisterProperties;
import com.steven.solomon.job.xxl.service.XxlJobService;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnBean(XxlJobService.class)
public class XxlJobHealthAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "xxlJobAdminHealthIndicator")
  public HealthIndicator xxlJobAdminHealthIndicator(
      XxlJobService service, XxlJobRegisterProperties registerProperties) {
    return new XxlJobAdminHealthIndicator(service, registerProperties);
  }
}
