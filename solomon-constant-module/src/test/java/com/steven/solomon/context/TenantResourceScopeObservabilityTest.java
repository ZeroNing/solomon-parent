package com.steven.solomon.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantResourceScopeObservabilityTest {

  @AfterEach
  void tearDown() {
    TenantResourceScope.clearObserversForTest();
  }

  @Test
  void shouldNotifyObserverForBindAndClear() throws Exception {
    AtomicInteger count = new AtomicInteger();
    AtomicReference<String> bindOutcome = new AtomicReference<>();
    TenantResourceScope.registerObserver((tenantCode, binderName, phase, outcome, durationNanos) -> {
      count.incrementAndGet();
      if ("bind".equals(phase)) {
        bindOutcome.set(tenantCode + ":" + binderName + ":" + outcome);
      }
      assertThat(durationNanos).isGreaterThanOrEqualTo(0);
    });

    TenantRequestBinder binder = new TestBinder();
    try (TenantResourceScope ignored = TenantResourceScope.open("tenant-a", List.of(binder))) {
      assertThat(count).hasValue(1);
      assertThat(bindOutcome).hasValue("tenant-a:TestBinder:success");
    }

    assertThat(count).hasValue(2);
  }

  @Test
  void shouldIgnoreObserverFailure() throws Exception {
    TenantResourceScope.registerObserver((tenantCode, binderName, phase, outcome, durationNanos) -> {
      throw new IllegalStateException("metrics down");
    });

    try (TenantResourceScope ignored = TenantResourceScope.open("tenant-a", List.of(new TestBinder()))) {
      assertThat(ignored).isNotNull();
    }
  }

  private static class TestBinder implements TenantRequestBinder {

    @Override
    public void bind(String tenantCode) {
    }

    @Override
    public void clear() {
    }
  }
}
