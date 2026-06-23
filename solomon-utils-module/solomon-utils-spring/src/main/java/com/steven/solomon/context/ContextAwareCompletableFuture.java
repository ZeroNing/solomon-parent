package com.steven.solomon.context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class ContextAwareCompletableFuture {

  private ContextAwareCompletableFuture() {
  }

  public static CompletableFuture<Void> runAsync(Runnable runnable) {
    return CompletableFuture.runAsync(RequestContextSnapshot.capture().wrap(runnable));
  }

  public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
    return CompletableFuture.runAsync(RequestContextSnapshot.capture().wrap(runnable), executor);
  }

  public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(RequestContextSnapshot.capture().wrap(supplier));
  }

  public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
    return CompletableFuture.supplyAsync(RequestContextSnapshot.capture().wrap(supplier), executor);
  }

  public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
    CompletableFuture<T> future = new CompletableFuture<>();
    completeExceptionally(future, throwable, RequestContextSnapshot.capture());
    return future;
  }

  public static <T> boolean complete(
      CompletableFuture<T> future, T value, RequestContextSnapshot snapshot) {
    return snapshot.get(() -> future.complete(value));
  }

  public static boolean completeExceptionally(
      CompletableFuture<?> future, Throwable throwable, RequestContextSnapshot snapshot) {
    return snapshot.get(() -> future.completeExceptionally(throwable));
  }
}
