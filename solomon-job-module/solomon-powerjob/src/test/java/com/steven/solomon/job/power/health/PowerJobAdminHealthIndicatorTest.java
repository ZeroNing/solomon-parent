package com.steven.solomon.job.power.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.steven.solomon.job.power.properties.PowerJobRegisterProperties;
import com.steven.solomon.job.power.service.PowerJobService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class PowerJobAdminHealthIndicatorTest {

  @Test
  void shouldBeUpWithoutLoginWhenRegistrationDisabled() throws Exception {
    PowerJobService service = org.mockito.Mockito.mock(PowerJobService.class);
    PowerJobRegisterProperties properties = new PowerJobRegisterProperties();

    Health health = new PowerJobAdminHealthIndicator(service, properties).health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsEntry("registrationEnabled", false);
    verify(service, never()).login();
  }

  @Test
  void shouldBeDownWhenLoginFails() throws Exception {
    PowerJobService service = org.mockito.Mockito.mock(PowerJobService.class);
    PowerJobRegisterProperties properties = new PowerJobRegisterProperties();
    properties.setEnabled(true);
    when(service.login()).thenThrow(new IllegalStateException("admin unavailable"));

    Health health = new PowerJobAdminHealthIndicator(service, properties).health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("registrationEnabled", true);
  }
}
