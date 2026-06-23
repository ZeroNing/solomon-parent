package com.steven.solomon.job.power.config;

import com.steven.solomon.job.power.health.PowerJobAdminHealthIndicator;
import com.steven.solomon.job.power.properties.PowerJobRegisterProperties;
import com.steven.solomon.job.power.service.PowerJobService;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnBean(PowerJobService.class)
public class PowerJobHealthAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "powerJobAdminHealthIndicator")
  public HealthIndicator powerJobAdminHealthIndicator(
      PowerJobService service, PowerJobRegisterProperties registerProperties) {
    return new PowerJobAdminHealthIndicator(service, registerProperties);
  }
}
