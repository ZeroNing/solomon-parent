package com.steven.solomon.reliability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

  @Test
  void shouldOpenAfterFailureThresholdAndFailFast() {
    AtomicLong now = new AtomicLong(1000);
    AtomicInteger calls = new AtomicInteger();
    CircuitBreaker circuitBreaker = new CircuitBreaker(now::get);
    CircuitBreaker.Options options = CircuitBreaker.Options.of(2, 1000);

    assertThatThrownBy(() -> circuitBreaker.execute("demo", options, () -> {
      calls.incrementAndGet();
      throw new IOException("down");
    })).isInstanceOf(IOException.class);
    assertThat(circuitBreaker.state()).isEqualTo(CircuitBreaker.State.CLOSED);

    assertThatThrownBy(() -> circuitBreaker.execute("demo", options, () -> {
      calls.incrementAndGet();
      throw new IOException("down");
    })).isInstanceOf(IOException.class);
    assertThat(circuitBreaker.state()).isEqualTo(CircuitBreaker.State.OPEN);

    assertThatThrownBy(() -> circuitBreaker.execute("demo", options, () -> {
      calls.incrementAndGet();
      return "ignored";
    })).isInstanceOf(CircuitBreaker.CircuitBreakerOpenException.class);
    assertThat(calls).hasValue(2);
  }

  @Test
  void shouldCloseAfterSuccessfulHalfOpenProbe() throws Exception {
    AtomicLong now = new AtomicLong(1000);
    CircuitBreaker circuitBreaker = new CircuitBreaker(now::get);
    CircuitBreaker.Options options = CircuitBreaker.Options.of(1, 1000);

    assertThatThrownBy(() -> circuitBreaker.execute("demo", options, () -> {
      throw new IOException("down");
    })).isInstanceOf(IOException.class);
    assertThat(circuitBreaker.state()).isEqualTo(CircuitBreaker.State.OPEN);

    now.addAndGet(1000);
    String result = circuitBreaker.execute("demo", options, () -> "ok");

    assertThat(result).isEqualTo("ok");
    assertThat(circuitBreaker.state()).isEqualTo(CircuitBreaker.State.CLOSED);
  }

  @Test
  void shouldReopenWhenHalfOpenProbeFails() {
    AtomicLong now = new AtomicLong(1000);
    CircuitBreaker circuitBreaker = new CircuitBreaker(now::get);
    CircuitBreaker.Options options = CircuitBreaker.Options.of(1, 1000);

    assertThatThrownBy(() -> circuitBreaker.execute("demo", options, () -> {
      throw new IOException("down");
    })).isInstanceOf(IOException.class);

    now.addAndGet(1000);
    assertThatThrownBy(() -> circuitBreaker.execute("demo", options, () -> {
      throw new IOException("still down");
    })).isInstanceOf(IOException.class);

    assertThat(circuitBreaker.state()).isEqualTo(CircuitBreaker.State.OPEN);
  }
}
