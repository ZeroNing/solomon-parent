package com.steven.solomon.context;

public interface TenantSwitchObserver {

  void record(String tenantCode, String binderName, String phase, String outcome, long durationNanos);
}
