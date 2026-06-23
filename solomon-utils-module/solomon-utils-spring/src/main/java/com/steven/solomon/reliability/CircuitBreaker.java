package com.steven.solomon.reliability;

import java.util.Objects;
import java.util.concurrent.Callable;

public class CircuitBreaker {

  private final Clock clock;
  private State state = State.CLOSED;
  private int failureCount;
  private long openedAtMillis;

  public CircuitBreaker() {
    this(System::currentTimeMillis);
  }

  CircuitBreaker(Clock clock) {
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
  }

  public <T> T execute(String operation, Options options, Callable<T> task) throws Exception {
    Options effectiveOptions = options == null ? Options.defaults() : options.normalized();
    beforeCall(operation, effectiveOptions);
    try {
      T result = task.call();
      onSuccess();
      return result;
    } catch (Exception ex) {
      onFailure(effectiveOptions);
      throw ex;
    }
  }

  public synchronized State state() {
    return state;
  }

  private synchronized void beforeCall(String operation, Options options) {
    if (state != State.OPEN) {
      return;
    }
    long elapsedMillis = clock.currentTimeMillis() - openedAtMillis;
    if (elapsedMillis >= options.openDurationMillis()) {
      state = State.HALF_OPEN;
      return;
    }
    throw new CircuitBreakerOpenException(operation, options.openDurationMillis() - elapsedMillis);
  }

  private synchronized void onSuccess() {
    state = State.CLOSED;
    failureCount = 0;
    openedAtMillis = 0;
  }

  private synchronized void onFailure(Options options) {
    if (state == State.HALF_OPEN) {
      open();
      return;
    }
    failureCount++;
    if (failureCount >= options.failureThreshold()) {
      open();
    }
  }

  private void open() {
    state = State.OPEN;
    failureCount = 0;
    openedAtMillis = clock.currentTimeMillis();
  }

  @FunctionalInterface
  interface Clock {
    long currentTimeMillis();
  }

  public enum State {
    CLOSED,
    OPEN,
    HALF_OPEN
  }

  public record Options(int failureThreshold, long openDurationMillis) {

    public static Options defaults() {
      return new Options(5, 30000);
    }

    public static Options of(int failureThreshold, long openDurationMillis) {
      return new Options(failureThreshold, openDurationMillis);
    }

    Options normalized() {
      return new Options(Math.max(1, failureThreshold), Math.max(1, openDurationMillis));
    }
  }

  public static class CircuitBreakerOpenException extends RuntimeException {

    public CircuitBreakerOpenException(String operation, long remainingOpenMillis) {
      super("Circuit breaker is open for operation " + operation
          + ", remainingOpenMillis=" + Math.max(0, remainingOpenMillis));
    }
  }
}
