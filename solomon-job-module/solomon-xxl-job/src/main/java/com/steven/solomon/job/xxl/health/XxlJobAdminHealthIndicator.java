package com.steven.solomon.job.xxl.health;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.job.xxl.properties.XxlJobRegisterProperties;
import com.steven.solomon.job.xxl.service.XxlJobService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class XxlJobAdminHealthIndicator implements HealthIndicator {

  private final XxlJobService service;
  private final XxlJobRegisterProperties registerProperties;

  public XxlJobAdminHealthIndicator(
      XxlJobService service, XxlJobRegisterProperties registerProperties) {
    this.service = service;
    this.registerProperties = registerProperties;
  }

  @Override
  public Health health() {
    if (!registerProperties.getEnabled()) {
      return Health.up()
          .withDetail("component", "solomon-xxl-job")
          .withDetail("provider", "xxl-job")
          .withDetail("registrationEnabled", false)
          .build();
    }
    try {
      String cookie = service.login();
      return Health.up()
          .withDetail("component", "solomon-xxl-job")
          .withDetail("provider", "xxl-job")
          .withDetail("registrationEnabled", true)
          .withDetail("authenticated", ObjectUtil.isNotEmpty(cookie))
          .build();
    } catch (Exception ex) {
      return Health.down(ex)
          .withDetail("component", "solomon-xxl-job")
          .withDetail("provider", "xxl-job")
          .withDetail("registrationEnabled", true)
          .build();
    }
  }
}
