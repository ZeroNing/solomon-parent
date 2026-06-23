package com.steven.solomon.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class TenantSwitchObservabilityAutoConfigurationTest {

  @Test
  void shouldRecordTenantSwitchMetrics() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    TenantSwitchObservabilityAutoConfiguration.MicrometerTenantSwitchObserver observer =
        new TenantSwitchObservabilityAutoConfiguration.MicrometerTenantSwitchObserver(meterRegistry);

    observer.record("tenant-a", "DataSourceBinder", "bind", "success", 1000);

    assertThat(meterRegistry.counter(
        "solomon.tenant.switch.total",
        "tenant", "tenant-a",
        "binder", "DataSourceBinder",
        "phase", "bind",
        "outcome", "success").count()).isEqualTo(1.0);
    assertThat(meterRegistry.timer(
        "solomon.tenant.switch.duration",
        "tenant", "tenant-a",
        "binder", "DataSourceBinder",
        "phase", "bind",
        "outcome", "success").count()).isEqualTo(1);
  }
}
