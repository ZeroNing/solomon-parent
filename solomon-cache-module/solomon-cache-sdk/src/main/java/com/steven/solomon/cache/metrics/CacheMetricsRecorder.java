package com.steven.solomon.cache.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;

public class CacheMetricsRecorder {

  private static final String OUTCOME_SUCCESS = "success";
  private static final String OUTCOME_ERROR = "error";
  private static final String GROUP_NONE = "none";

  private final MeterRegistry meterRegistry;
  private final String totalMetricName;
  private final String durationMetricName;
  private final String durationDescription;

  public CacheMetricsRecorder(
      MeterRegistry meterRegistry,
      String totalMetricName,
      String durationMetricName,
      String durationDescription) {
    this.meterRegistry = meterRegistry;
    this.totalMetricName = totalMetricName;
    this.durationMetricName = durationMetricName;
    this.durationDescription = durationDescription;
  }

  public <T> T record(String operation, String group, Supplier<T> action) {
    if (meterRegistry == null) {
      return action.get();
    }
    Timer.Sample sample = Timer.start(meterRegistry);
    String outcome = OUTCOME_SUCCESS;
    try {
      return action.get();
    } catch (RuntimeException ex) {
      outcome = OUTCOME_ERROR;
      throw ex;
    } finally {
      String groupTag = safeTag(group);
      sample.stop(Timer.builder(durationMetricName)
          .description(durationDescription)
          .tag("operation", operation)
          .tag("group", groupTag)
          .tag("outcome", outcome)
          .register(meterRegistry));
      meterRegistry.counter(totalMetricName,
          "operation", operation,
          "group", groupTag,
          "outcome", outcome).increment();
    }
  }

  private String safeTag(String value) {
    return value == null || value.isBlank() ? GROUP_NONE : value;
  }
}
