package com.steven.solomon.mq.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MessageIdempotencyExecutorTest {

  @Test
  void shouldMarkMessageConsumedAfterSuccess() throws Exception {
    InMemoryMessageIdempotencyStore store = new InMemoryMessageIdempotencyStore();
    MessageIdempotencyExecutor executor =
        new MessageIdempotencyExecutor(store, Duration.ofMinutes(1), Duration.ofMinutes(1));
    AtomicInteger calls = new AtomicInteger();

    String result = executor.execute("msg-1", () -> {
      calls.incrementAndGet();
      return "ok";
    });

    assertThat(result).isEqualTo("ok");
    assertThat(calls).hasValue(1);
    assertThatThrownBy(() -> executor.execute("msg-1", () -> {
      calls.incrementAndGet();
      return "duplicate";
    })).isInstanceOf(DuplicateMessageException.class);
    assertThat(calls).hasValue(1);
  }

  @Test
  void shouldReleaseProcessingKeyAfterFailure() {
    InMemoryMessageIdempotencyStore store = new InMemoryMessageIdempotencyStore();
    MessageIdempotencyExecutor executor =
        new MessageIdempotencyExecutor(store, Duration.ofMinutes(1), Duration.ofMinutes(1));
    AtomicInteger calls = new AtomicInteger();

    assertThatThrownBy(() -> executor.execute("msg-1", () -> {
      calls.incrementAndGet();
      throw new IllegalStateException("temporary");
    })).isInstanceOf(IllegalStateException.class);

    assertThatThrownBy(() -> executor.execute("msg-1", () -> {
      calls.incrementAndGet();
      throw new IllegalStateException("temporary");
    })).isInstanceOf(IllegalStateException.class);
    assertThat(calls).hasValue(2);
  }
}
