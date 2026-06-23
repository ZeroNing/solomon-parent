package com.steven.solomon.mq.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.mq.idempotency.MessageIdempotencyExecutor;
import com.steven.solomon.mq.idempotency.MessageIdempotencyStore;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.util.Throwables;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MqIdempotencyAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(MqIdempotencyAutoConfiguration.class));

  @Test
  void shouldNotCreateExecutorByDefault() {
    contextRunner.run(context ->
        assertThat(context).doesNotHaveBean(MessageIdempotencyExecutor.class));
  }

  @Test
  void shouldCreateExecutorWhenEnabled() {
    contextRunner
        .withPropertyValues("solomon.mq.idempotency.enabled=true")
        .run(context -> {
          assertThat(context).hasSingleBean(MessageIdempotencyStore.class);
          assertThat(context).hasSingleBean(MessageIdempotencyExecutor.class);
        });
  }

  @Test
  void shouldUseCustomStoreWhenProvided() {
    CustomStore store = new CustomStore();
    contextRunner
        .withBean(MessageIdempotencyStore.class, () -> store)
        .withPropertyValues("solomon.mq.idempotency.enabled=true")
        .run(context -> {
          assertThat(context).hasSingleBean(MessageIdempotencyStore.class);
          assertThat(context.getBean(MessageIdempotencyStore.class)).isSameAs(store);
          assertThat(context).hasSingleBean(MessageIdempotencyExecutor.class);
        });
  }

  @Test
  void shouldFailFastWhenTtlIsInvalid() {
    contextRunner
        .withPropertyValues(
            "solomon.mq.idempotency.enabled=true",
            "solomon.mq.idempotency.processing-ttl=0ms")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(Throwables.getRootCause(context.getStartupFailure())).hasMessageContaining(
              "solomon.mq.idempotency.processing-ttl must be positive");
        });
  }

  private static class CustomStore implements MessageIdempotencyStore {

    private final AtomicReference<String> consumed = new AtomicReference<>();

    @Override
    public boolean tryAcquire(String key, Duration ttl) {
      return !key.equals(consumed.get());
    }

    @Override
    public void markConsumed(String key, Duration ttl) {
      consumed.set(key);
    }

    @Override
    public void release(String key) {
      consumed.compareAndSet(key, null);
    }
  }
}
