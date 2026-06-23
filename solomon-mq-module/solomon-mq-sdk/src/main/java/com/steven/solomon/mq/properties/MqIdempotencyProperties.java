package com.steven.solomon.mq.properties;

import jakarta.validation.constraints.AssertTrue;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("solomon.mq.idempotency")
@Validated
public class MqIdempotencyProperties {

  private boolean enabled = false;

  private Duration processingTtl = Duration.ofMinutes(10);

  private Duration consumedTtl = Duration.ofDays(1);

  @AssertTrue(message = "solomon.mq.idempotency.processing-ttl must be positive")
  public boolean isProcessingTtlValid() {
    return processingTtl != null && !processingTtl.isZero() && !processingTtl.isNegative();
  }

  @AssertTrue(message = "solomon.mq.idempotency.consumed-ttl must be positive")
  public boolean isConsumedTtlValid() {
    return consumedTtl != null && !consumedTtl.isZero() && !consumedTtl.isNegative();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Duration getProcessingTtl() {
    return processingTtl;
  }

  public void setProcessingTtl(Duration processingTtl) {
    this.processingTtl = processingTtl;
  }

  public Duration getConsumedTtl() {
    return consumedTtl;
  }

  public void setConsumedTtl(Duration consumedTtl) {
    this.consumedTtl = consumedTtl;
  }
}
