package com.steven.solomon.reliability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryExecutorTest {

  @Test
  void shouldRetryUntilSuccess() throws Exception {
    AtomicInteger attempts = new AtomicInteger();
    RetryExecutor executor = new RetryExecutor(millis -> {
    });

    String result = executor.execute("demo", RetryExecutor.RetryOptions.of(3, 10), () -> {
      if (attempts.incrementAndGet() < 3) {
        throw new IOException("temporary");
      }
      return "ok";
    });

    assertThat(result).isEqualTo("ok");
    assertThat(attempts).hasValue(3);
  }

  @Test
  void shouldStopWhenExceptionIsNotRetryable() {
    AtomicInteger attempts = new AtomicInteger();
    RetryExecutor executor = new RetryExecutor(millis -> {
    });
    RetryExecutor.RetryOptions options =
        new RetryExecutor.RetryOptions(3, 10, ex -> !(ex instanceof IllegalArgumentException));

    assertThatThrownBy(() -> executor.execute("demo", options, () -> {
      attempts.incrementAndGet();
      throw new IllegalArgumentException("bad input");
    })).isInstanceOf(IllegalArgumentException.class);
    assertThat(attempts).hasValue(1);
  }

  @Test
  void shouldRestoreInterruptedFlagWhenBackoffIsInterrupted() {
    RetryExecutor executor = new RetryExecutor(millis -> {
      throw new InterruptedException("interrupted");
    });

    assertThatThrownBy(() -> executor.execute("demo", RetryExecutor.RetryOptions.of(2, 10), () -> {
      throw new IOException("temporary");
    })).isInstanceOf(InterruptedException.class);
    assertThat(Thread.currentThread().isInterrupted()).isTrue();
    Thread.interrupted();
  }
}
