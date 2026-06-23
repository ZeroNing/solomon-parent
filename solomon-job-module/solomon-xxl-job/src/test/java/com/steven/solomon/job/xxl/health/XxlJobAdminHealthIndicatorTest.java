package com.steven.solomon.job.xxl.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.steven.solomon.job.xxl.properties.XxlJobRegisterProperties;
import com.steven.solomon.job.xxl.service.XxlJobService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class XxlJobAdminHealthIndicatorTest {

  @Test
  void shouldBeUpWithoutLoginWhenRegistrationDisabled() throws Exception {
    XxlJobService service = org.mockito.Mockito.mock(XxlJobService.class);
    XxlJobRegisterProperties properties = new XxlJobRegisterProperties();

    Health health = new XxlJobAdminHealthIndicator(service, properties).health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsEntry("registrationEnabled", false);
    verify(service, never()).login();
  }

  @Test
  void shouldBeDownWhenLoginFails() throws Exception {
    XxlJobService service = org.mockito.Mockito.mock(XxlJobService.class);
    XxlJobRegisterProperties properties = new XxlJobRegisterProperties();
    properties.setEnabled(true);
    when(service.login()).thenThrow(new IllegalStateException("admin unavailable"));

    Health health = new XxlJobAdminHealthIndicator(service, properties).health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("registrationEnabled", true);
  }
}
