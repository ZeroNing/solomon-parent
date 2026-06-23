package com.steven.solomon.job.power.health;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.job.power.properties.PowerJobRegisterProperties;
import com.steven.solomon.job.power.service.PowerJobService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class PowerJobAdminHealthIndicator implements HealthIndicator {

  private final PowerJobService service;
  private final PowerJobRegisterProperties registerProperties;

  public PowerJobAdminHealthIndicator(
      PowerJobService service, PowerJobRegisterProperties registerProperties) {
    this.service = service;
    this.registerProperties = registerProperties;
  }

  @Override
  public Health health() {
    if (!registerProperties.getEnabled()) {
      return Health.up()
          .withDetail("component", "solomon-powerjob")
          .withDetail("provider", "powerjob")
          .withDetail("registrationEnabled", false)
          .build();
    }
    try {
      String token = service.login();
      return Health.up()
          .withDetail("component", "solomon-powerjob")
          .withDetail("provider", "powerjob")
          .withDetail("registrationEnabled", true)
          .withDetail("authenticated", ObjectUtil.isNotEmpty(token))
          .build();
    } catch (Exception ex) {
      return Health.down(ex)
          .withDetail("component", "solomon-powerjob")
          .withDetail("provider", "powerjob")
          .withDetail("registrationEnabled", true)
          .build();
    }
  }
}
