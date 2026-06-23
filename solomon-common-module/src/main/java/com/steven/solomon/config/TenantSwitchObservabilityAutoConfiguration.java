package com.steven.solomon.config;

import com.steven.solomon.context.TenantResourceScope;
import com.steven.solomon.context.TenantSwitchObserver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnProperty(prefix = "solomon.tenant.observability", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class TenantSwitchObservabilityAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public TenantSwitchObserver solomonTenantSwitchObserver(MeterRegistry meterRegistry) {
    return new MicrometerTenantSwitchObserver(meterRegistry);
  }

  @Bean
  public TenantSwitchObserverRegistration solomonTenantSwitchObserverRegistration(
      TenantSwitchObserver observer) {
    return new TenantSwitchObserverRegistration(observer);
  }

  static class TenantSwitchObserverRegistration implements DisposableBean {

    private final TenantSwitchObserver observer;

    TenantSwitchObserverRegistration(TenantSwitchObserver observer) {
      this.observer = observer;
      TenantResourceScope.registerObserver(observer);
    }

    @Override
    public void destroy() {
      TenantResourceScope.unregisterObserver(observer);
    }
  }

  static class MicrometerTenantSwitchObserver implements TenantSwitchObserver {

    private final MeterRegistry meterRegistry;

    MicrometerTenantSwitchObserver(MeterRegistry meterRegistry) {
      this.meterRegistry = meterRegistry;
    }

    @Override
    public void record(String tenantCode, String binderName, String phase, String outcome, long durationNanos) {
      Tags tags = Tags.of(
          "tenant", safeTag(tenantCode),
          "binder", safeTag(binderName),
          "phase", safeTag(phase),
          "outcome", safeTag(outcome));
      meterRegistry.counter("solomon.tenant.switch.total", tags).increment();
      Timer.builder("solomon.tenant.switch.duration")
          .tags(tags)
          .register(meterRegistry)
          .record(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    private String safeTag(String value) {
      return value == null || value.isBlank() ? "unknown" : value;
    }
  }
}
