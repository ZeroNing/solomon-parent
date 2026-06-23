package com.steven.solomon.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ContextAwareCompletableFutureTest {

  @AfterEach
  void tearDown() {
    ExceptionUtil.requestId.remove();
    RequestHeaderHolder.remove();
  }

  @Test
  void shouldPropagateContextToCompletableFutureExecutor() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      ExceptionUtil.requestId.set("request-future");
      RequestHeaderHolder.setTenantCode("tenant-future");

      CompletableFuture<String> future = ContextAwareCompletableFuture.supplyAsync(
          () -> ExceptionUtil.requestId.get() + ":" + RequestHeaderHolder.getTenantCode(),
          executor);

      assertThat(future.join()).isEqualTo("request-future:tenant-future");
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void shouldRestoreContextWhenCompletingFutureOnCallbackThread() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      ExceptionUtil.requestId.set("request-callback");
      RequestHeaderHolder.setTenantCode("tenant-callback");
      RequestContextSnapshot snapshot = RequestContextSnapshot.capture();
      CompletableFuture<Void> future = new CompletableFuture<>();
      AtomicReference<String> context = new AtomicReference<>();

      future.thenRun(() -> context.set(
          ExceptionUtil.requestId.get() + ":" + RequestHeaderHolder.getTenantCode()));
      ExceptionUtil.requestId.remove();
      RequestHeaderHolder.remove();

      executor.submit(() -> ContextAwareCompletableFuture.complete(future, null, snapshot)).get();

      assertThat(context).hasValue("request-callback:tenant-callback");
    } finally {
      executor.shutdownNow();
    }
  }
}
