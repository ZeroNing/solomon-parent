package com.steven.solomon.reliability;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class RetryExecutor {

  private final Sleeper sleeper;

  public RetryExecutor() {
    this(Thread::sleep);
  }

  RetryExecutor(Sleeper sleeper) {
    this.sleeper = Objects.requireNonNull(sleeper, "sleeper must not be null");
  }

  public <T> T execute(String operation, RetryOptions options, Callable<T> task) throws Exception {
    RetryOptions effectiveOptions = options == null ? RetryOptions.defaults() : options.normalized();
    int attempt = 0;
    while (true) {
      attempt++;
      try {
        return task.call();
      } catch (Exception ex) {
        if (attempt >= effectiveOptions.maxAttempts()
            || !effectiveOptions.retryableException().test(ex)) {
          throw ex;
        }
        long backoffMillis = effectiveOptions.backoffMillis() * attempt;
        if (backoffMillis > 0) {
          try {
            sleeper.sleep(backoffMillis);
          } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw interrupted;
          }
        }
      }
    }
  }

  @FunctionalInterface
  interface Sleeper {
    void sleep(long millis) throws InterruptedException;
  }

  public record RetryOptions(
      int maxAttempts,
      long backoffMillis,
      Predicate<Exception> retryableException) {

    public static RetryOptions defaults() {
      return new RetryOptions(1, 0, ex -> true);
    }

    public static RetryOptions of(int maxAttempts, long backoffMillis) {
      return new RetryOptions(maxAttempts, backoffMillis, ex -> true);
    }

    RetryOptions normalized() {
      int attempts = Math.max(1, maxAttempts);
      long backoff = Math.max(0, backoffMillis);
      Predicate<Exception> predicate = retryableException == null ? ex -> true : retryableException;
      return new RetryOptions(attempts, backoff, predicate);
    }
  }
}
